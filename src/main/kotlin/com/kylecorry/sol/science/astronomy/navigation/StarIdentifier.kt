package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.optics.PinholeCamera
import com.kylecorry.sol.units.Coordinate
import kotlin.math.abs

/**
 * Identifies stars in a photo by matching pairs of the brightest image stars to the catalog (see CatalogMatcher).
 * The matches are then used to refine the focal length, distortion and rotation.
 */
internal class StarIdentifier(private val settings: StarIdentifierSettings = StarIdentifierSettings()) {

    private class FocalHypothesis(val hypothesis: CatalogMatcher.Hypothesis, val focalLength: Double)

    fun identify(image: SkyImage, approximateLocation: Coordinate): StarIdentification? {
        if (image.stars.size < 2) return null
        val catalog = SkyCatalog.getPositions(
            SkyCatalog.getBodies(settings.maxMagnitude, settings.includePlanets),
            image.time,
            approximateLocation,
            settings.withRefraction,
            -settings.maxZenithOffsetDegrees
        )

        var best: FocalHypothesis? = null
        for (limit in listOf(settings.seedMagnitude, settings.maxMagnitude).distinct()) {
            if ((best?.hypothesis?.score ?: 0) >= settings.minMatches) break
            val matcher = createMatcher(image, catalog.filter { it.body.magnitude <= limit })
            val candidate = findBestHypothesis(image, matcher)
            if (candidate != null && candidate.hypothesis.isBetterThan(best?.hypothesis)) {
                best = candidate
            }
        }

        val minMatches = settings.minMatches.coerceAtMost(image.stars.size)
        return best?.takeIf { it.hypothesis.score >= minMatches }?.let {
            val camera = image.camera.copy(focalLength = it.focalLength)
            refine(image, camera, it.hypothesis.rotation, createMatcher(image, catalog))
        }
    }

    /**
     * Fits the rotation from the camera to the sky for already identified stars at a location
     */
    fun fitRotation(image: SkyImage, identification: StarIdentification, location: Coordinate): Rotation {
        val rays = identification.stars.map { identification.camera.toRay(it.star.x, it.star.y) }
        val targets = SkyCatalog.getPositions(
            identification.stars.map { it.body },
            image.time,
            location,
            settings.withRefraction
        ).map { it.vector }
        return Rotation.fit(rays, targets)
    }

    private fun createMatcher(image: SkyImage, catalog: List<BodyPosition>): CatalogMatcher {
        return CatalogMatcher(catalog, image.up, settings.maxZenithOffsetDegrees, settings.inlierToleranceDegrees)
    }

    private fun findBestHypothesis(image: SkyImage, matcher: CatalogMatcher): FocalHypothesis? {
        val seeds = image.stars.sortedByDescending { it.brightness }.take(settings.seedCount)
        var best: FocalHypothesis? = null
        for (a in seeds.indices) {
            for (b in a + 1 until seeds.size) {
                for (candidate in getHypotheses(image, seeds, a, b, matcher)) {
                    if (candidate.hypothesis.isBetterThan(best?.hypothesis)) {
                        best = candidate
                    }
                }
            }
        }
        return best
    }

    private fun getHypotheses(
        image: SkyImage,
        seeds: List<ImageStar>,
        a: Int,
        b: Int,
        matcher: CatalogMatcher
    ): List<FocalHypothesis> {
        val f0 = image.camera.focalLength
        val observed = CatalogMatcher.angle(
            image.camera.toRay(seeds[a].x, seeds[a].y),
            image.camera.toRay(seeds[b].x, seeds[b].y)
        )
        if (observed < MIN_OBSERVED_SEPARATION) return emptyList()
        val tolerance = settings.pairToleranceDegrees + observed * settings.focalUncertainty
        return matcher.getPairs(observed, tolerance).flatMap { pair ->
            // Angles scale roughly inversely with focal length
            val focal = (f0 * observed / pair.separation).coerceIn(
                f0 * (1 - settings.focalUncertainty),
                f0 * (1 + settings.focalUncertainty)
            )
            val camera = image.camera.copy(focalLength = focal)
            val rays = seeds.map { camera.toRay(it.x, it.y) }
            matcher.getHypotheses(rays, a, b, pair).map { FocalHypothesis(it, focal) }
        }
    }

    private fun refine(
        image: SkyImage,
        initialCamera: PinholeCamera,
        initialRotation: Rotation,
        matcher: CatalogMatcher
    ): StarIdentification? {
        var camera = initialCamera
        var rays = getRays(image, camera)
        var matches = matcher.associate(rays, initialRotation)

        var iteration = 0
        while (iteration < REFINE_ITERATIONS && matches.size >= 2) {
            if (matches.size >= MIN_LENS_FIT_MATCHES && (settings.fitFocalLength || settings.fitDistortion)) {
                camera = fitLens(image, camera, matches, matcher)
                rays = getRays(image, camera)
            }
            matches = matcher.refine(rays, matches, MIN_INLIER_TOLERANCE).second
            iteration++
        }

        if (matches.size < 2) return null
        val rotation = matcher.fitRotation(rays, matches)
        val stars = matches.map {
            IdentifiedStar(it.index, image.stars[it.index], matcher.catalog[it.catalogIndex].body)
        }
        return StarIdentification(stars, camera, rotation, matcher.rms(rays, matches, rotation))
    }

    private fun getRays(image: SkyImage, camera: PinholeCamera): List<Vector3Precise> {
        return image.stars.map { camera.toRay(it.x, it.y) }
    }

    /**
     * A coordinate descent on the focal length and distortion that minimizes the fit error
     */
    private fun fitLens(
        image: SkyImage,
        start: PinholeCamera,
        matches: List<CatalogMatcher.Match>,
        matcher: CatalogMatcher
    ): PinholeCamera {
        val stars = matches.map { image.stars[it.index] }
        val targets = matches.map { matcher.catalog[it.catalogIndex].vector }
        val f0 = image.camera.focalLength
        val focalRange = (f0 * (1 - settings.focalUncertainty * LENS_SEARCH_SCALE))..
                (f0 * (1 + settings.focalUncertainty * LENS_SEARCH_SCALE))
        val fitDistortion = settings.fitDistortion && matches.size >= MIN_DISTORTION_FIT_MATCHES

        fun cost(f: Double, k1: Double): Double {
            val camera = start.copy(focalLength = f, k1 = k1)
            val rays = stars.map { camera.toRay(it.x, it.y) }
            return CatalogMatcher.rms(rays, targets, Rotation.fit(rays, targets))
        }

        var f = start.focalLength
        var k1 = start.k1
        var fStep = f0 * INITIAL_FOCAL_STEP
        var kStep = INITIAL_K1_STEP
        var bestCost = cost(f, k1)
        var iteration = 0
        while (iteration < MAX_LENS_ITERATIONS && fStep >= MIN_FOCAL_STEP) {
            val focalCandidates = if (settings.fitFocalLength) listOf(f - fStep, f + fStep) else emptyList()
            val newF = minimize(focalCandidates.filter { it in focalRange }, bestCost) { cost(it, k1) }
            newF?.let {
                f = it.first
                bestCost = it.second
            }
            val k1Candidates = if (fitDistortion) listOf(k1 - kStep, k1 + kStep) else emptyList()
            val newK1 = minimize(k1Candidates.filter { abs(it) <= MAX_K1 }, bestCost) { cost(f, it) }
            newK1?.let {
                k1 = it.first
                bestCost = it.second
            }
            if (newF == null && newK1 == null) {
                fStep /= 2
                kStep /= 2
            }
            iteration++
        }
        return start.copy(focalLength = f, k1 = k1)
    }

    companion object {
        /**
         * @return the candidate and its cost if any is lower than the current cost
         */
        private fun minimize(
            candidates: List<Double>,
            currentCost: Double,
            cost: (Double) -> Double
        ): Pair<Double, Double>? {
            return candidates.map { it to cost(it) }.filter { it.second < currentCost }.minByOrNull { it.second }
        }

        private const val MIN_OBSERVED_SEPARATION = 1.5
        private const val REFINE_ITERATIONS = 4
        private const val MIN_LENS_FIT_MATCHES = 4
        private const val MIN_DISTORTION_FIT_MATCHES = 5
        private const val MIN_INLIER_TOLERANCE = 0.05
        private const val LENS_SEARCH_SCALE = 1.5
        private const val INITIAL_FOCAL_STEP = 0.01
        private const val INITIAL_K1_STEP = 0.02
        private const val MIN_FOCAL_STEP = 0.01
        private const val MAX_K1 = 0.3
        private const val MAX_LENS_ITERATIONS = 60
    }
}
