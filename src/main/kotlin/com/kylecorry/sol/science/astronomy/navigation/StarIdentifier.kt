package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.optics.PinholeCamera
import com.kylecorry.sol.units.Coordinate
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Identifies stars in a photo. Every pair of bright image stars is compared with catalog pairs of
 * the same angular separation, each candidate gives a rotation (TRIAD), and the rotation that lines
 * up the most other stars wins. Only the angles between stars are used, so no compass is needed.
 * The matches are then used to refine the focal length, distortion and rotation.
 */
internal class StarIdentifier(private val settings: StarIdentifierSettings = StarIdentifierSettings()) {

    private class Hypothesis(val rotation: Rotation, val focalLength: Double, val score: Int, val residual: Double)

    private class Seeds(val indices: List<Int>, val stars: List<ImageStar>)

    fun identify(image: SkyImage, approximateLocation: Coordinate): StarIdentification? {
        if (image.stars.size < 2) return null
        val catalog = SkyCatalog.getPositions(
            SkyCatalog.getBodies(settings.maxMagnitude, settings.includePlanets),
            image.time,
            approximateLocation,
            settings.withRefraction,
            -settings.maxZenithOffsetDegrees
        )

        var best: Hypothesis? = null
        for (limit in listOf(settings.seedMagnitude, settings.maxMagnitude).distinct()) {
            if ((best?.score ?: 0) >= settings.minMatches) break
            val hypothesis = findBestHypothesis(image, catalog.filter { it.body.magnitude <= limit })
            if (hypothesis != null && isBetter(hypothesis, best)) {
                best = hypothesis
            }
        }

        return best?.takeIf { it.score >= settings.minMatches.coerceAtMost(image.stars.size) }?.let {
            refine(image, image.camera.copy(focalLength = it.focalLength), it.rotation, catalog)
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

    private fun findBestHypothesis(image: SkyImage, catalog: List<BodyPosition>): Hypothesis? {
        val indices = image.stars.indices.sortedByDescending { image.stars[it].brightness }.take(settings.seedCount)
        val seeds = Seeds(indices, indices.map { image.stars[it] })
        val pairs = CatalogPairs(catalog, MIN_PAIR_SEPARATION, MAX_PAIR_SEPARATION)
        var best: Hypothesis? = null
        for (a in seeds.indices.indices) {
            for (b in a + 1 until seeds.indices.size) {
                for (hypothesis in getHypotheses(image, seeds, a, b, pairs)) {
                    if (isBetter(hypothesis, best)) {
                        best = hypothesis
                    }
                }
            }
        }
        return best
    }

    /**
     * All hypotheses where seed stars a and b are a catalog pair of the same separation
     */
    private fun getHypotheses(image: SkyImage, seeds: Seeds, a: Int, b: Int, pairs: CatalogPairs): List<Hypothesis> {
        val f0 = image.camera.focalLength
        val observed = angle(
            image.camera.toRay(seeds.stars[a].x, seeds.stars[a].y),
            image.camera.toRay(seeds.stars[b].x, seeds.stars[b].y)
        )
        if (observed < MIN_OBSERVED_SEPARATION) return emptyList()
        val tolerance = settings.pairToleranceDegrees + observed * settings.focalUncertainty
        return pairs.near(observed, tolerance).flatMap { pair ->
            // Angles scale roughly inversely with focal length
            val focal = (f0 * observed / pair.separation).coerceIn(
                f0 * (1 - settings.focalUncertainty),
                f0 * (1 + settings.focalUncertainty)
            )
            val camera = image.camera.copy(focalLength = focal)
            val rays = seeds.stars.map { camera.toRay(it.x, it.y) }
            listOf(pair.first to pair.second, pair.second to pair.first).mapNotNull { (from, to) ->
                Rotation.triad(rays[a], rays[b], pairs.catalog[from].vector, pairs.catalog[to].vector)
                    ?.takeIf { isConsistentWithGravity(image, it) }
                    ?.let { score(it, rays, a to b, focal, pairs.catalog) }
            }
        }
    }

    /**
     * Counts the other seed stars that line up with a catalog body under the rotation
     */
    private fun score(
        rotation: Rotation,
        rays: List<Vector3Precise>,
        seedPair: Pair<Int, Int>,
        focalLength: Double,
        catalog: List<BodyPosition>
    ): Hypothesis {
        var score = 2
        var residual = 0.0
        for (k in rays.indices) {
            val isSeed = k == seedPair.first || k == seedPair.second
            val distance = if (isSeed) {
                Double.MAX_VALUE
            } else {
                CatalogPairs.nearest(rotation.rotate(rays[k]), catalog).second
            }
            if (distance <= settings.inlierToleranceDegrees) {
                score++
                residual += distance
            }
        }
        return Hypothesis(rotation, focalLength, score, residual)
    }

    private fun isConsistentWithGravity(image: SkyImage, rotation: Rotation): Boolean {
        val up = image.up ?: return true
        return angle(rotation.rotate(up), HorizonVectors.up) <= settings.maxZenithOffsetDegrees
    }

    private fun isBetter(candidate: Hypothesis, current: Hypothesis?): Boolean {
        if (current == null) return true
        return candidate.score > current.score ||
                (candidate.score == current.score && candidate.residual < current.residual)
    }

    private fun refine(
        image: SkyImage,
        initialCamera: PinholeCamera,
        initialRotation: Rotation,
        catalog: List<BodyPosition>
    ): StarIdentification? {
        var camera = initialCamera
        var rotation = initialRotation
        var matches = associate(image, camera, rotation, catalog, settings.inlierToleranceDegrees)
        val positions = catalog.associateBy { it.body }

        var iteration = 0
        while (iteration < REFINE_ITERATIONS && matches.size >= 2) {
            val targets = matches.map { match -> checkNotNull(positions[match.body]).vector }
            if (matches.size >= MIN_LENS_FIT_MATCHES && (settings.fitFocalLength || settings.fitDistortion)) {
                camera = fitLens(image, camera, matches, targets)
            }
            val rays = matches.map { match -> camera.toRay(match.star.x, match.star.y) }
            rotation = Rotation.fit(rays, targets)
            val tolerance = (rms(rays, targets, rotation) * INLIER_RMS_SCALE).coerceIn(
                MIN_INLIER_TOLERANCE,
                settings.inlierToleranceDegrees
            )
            matches = associate(image, camera, rotation, catalog, tolerance)
            iteration++
        }

        if (matches.size < 2) return null
        val targets = matches.map { checkNotNull(positions[it.body]).vector }
        val rays = matches.map { camera.toRay(it.star.x, it.star.y) }
        rotation = Rotation.fit(rays, targets)
        return StarIdentification(matches, camera, rotation, rms(rays, targets, rotation))
    }

    private fun associate(
        image: SkyImage,
        camera: PinholeCamera,
        rotation: Rotation,
        catalog: List<BodyPosition>,
        tolerance: Double
    ): List<IdentifiedStar> {
        val candidates = image.stars.mapIndexedNotNull { index, star ->
            val (nearest, distance) = CatalogPairs.nearest(rotation.rotate(camera.toRay(star.x, star.y)), catalog)
            if (nearest >= 0 && distance <= tolerance) Triple(index, catalog[nearest].body, distance) else null
        }
        // Each body can only be matched once, keep the closest
        return candidates.groupBy { it.second }.map { group -> group.value.minBy { it.third } }
            .map { IdentifiedStar(it.first, image.stars[it.first], it.second) }
    }

    /**
     * A coordinate descent on the focal length and distortion that minimizes the fit error
     */
    private fun fitLens(
        image: SkyImage,
        start: PinholeCamera,
        matches: List<IdentifiedStar>,
        targets: List<Vector3Precise>
    ): PinholeCamera {
        val f0 = image.camera.focalLength
        val focalRange = (f0 * (1 - settings.focalUncertainty * LENS_SEARCH_SCALE))..
                (f0 * (1 + settings.focalUncertainty * LENS_SEARCH_SCALE))
        val fitDistortion = settings.fitDistortion && matches.size >= MIN_DISTORTION_FIT_MATCHES

        fun cost(f: Double, k1: Double): Double {
            val camera = start.copy(focalLength = f, k1 = k1)
            val rays = matches.map { camera.toRay(it.star.x, it.star.y) }
            return rms(rays, targets, Rotation.fit(rays, targets))
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

        private const val MIN_PAIR_SEPARATION = 1.0
        private const val MAX_PAIR_SEPARATION = 130.0
        private const val MIN_OBSERVED_SEPARATION = 1.5
        private const val REFINE_ITERATIONS = 4
        private const val MIN_LENS_FIT_MATCHES = 4
        private const val MIN_DISTORTION_FIT_MATCHES = 5
        private const val MIN_INLIER_TOLERANCE = 0.05
        private const val INLIER_RMS_SCALE = 4
        private const val LENS_SEARCH_SCALE = 1.5
        private const val INITIAL_FOCAL_STEP = 0.01
        private const val INITIAL_K1_STEP = 0.02
        private const val MIN_FOCAL_STEP = 0.01
        private const val MAX_K1 = 0.3
        private const val MAX_LENS_ITERATIONS = 60

        internal fun angle(a: Vector3Precise, b: Vector3Precise): Double {
            return Math.toDegrees(acos((a.dot(b) / (a.magnitude() * b.magnitude())).coerceIn(-1.0, 1.0)))
        }

        internal fun rms(rays: List<Vector3Precise>, targets: List<Vector3Precise>, rotation: Rotation): Double {
            if (rays.isEmpty()) return 0.0
            val sum = rays.indices.sumOf {
                val d = angle(rotation.rotate(rays[it]), targets[it])
                d * d
            }
            return sqrt(sum / rays.size)
        }
    }
}
