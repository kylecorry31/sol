package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.astronomy.navigation.CatalogMatcher
import com.kylecorry.sol.science.astronomy.navigation.HorizonVectors
import com.kylecorry.sol.science.astronomy.navigation.SkyCatalog
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

/**
 * Identifies stars from altitude / azimuth readings by matching pairs of readings to the catalog (see
 * CatalogMatcher). Every identification is geometrically consistent with the others, and a constant compass error
 * doesn't matter.
 *
 * @param tolerance the fractional tolerance on angular separations
 * @param minMatches the minimum number of identified stars
 * @param minMagnitude the faintest catalog stars to consider
 * @param maxZenithOffset how far (degrees) the true location's zenith can be from the approximate
 * location's zenith, use 180 when the approximate location is unknown
 */
internal class PlateSolver(
    private val tolerance: Float,
    private val minMatches: Int,
    private val minMagnitude: Float,
    private val maxZenithOffset: Double = DEFAULT_MAX_ZENITH_OFFSET
) {

    fun solve(
        readings: List<CelestialObservation>,
        time: ZonedDateTime,
        approximateLocation: Coordinate
    ): List<DetectedStar> {
        if (readings.size < 2) return emptyList()
        val bodies = SkyCatalog.getBodies(minMagnitude, false)
        val catalog = SkyCatalog.getPositions(bodies, time, approximateLocation, true, -maxZenithOffset)
        val vectors = readings.map { HorizonVectors.toVector(it.azimuth.value.toDouble(), it.altitude.toDouble()) }
        val inlierTolerance = (tolerance * INLIER_DEGREES_PER_TOLERANCE).coerceAtLeast(MIN_INLIER_TOLERANCE)
        // The readings' up is the zenith at the true location, which must be near the approximate one
        val matcher = CatalogMatcher(catalog, HorizonVectors.up, maxZenithOffset, inlierTolerance)
        val best = findBestHypothesis(vectors, matcher)
            ?.takeIf { it.score >= minMatches.coerceAtMost(readings.size) }
        return best?.let { refine(readings, vectors, matcher, it.rotation, inlierTolerance) }.orEmpty()
    }

    private fun findBestHypothesis(vectors: List<Vector3Precise>, matcher: CatalogMatcher): CatalogMatcher.Hypothesis? {
        var best: CatalogMatcher.Hypothesis? = null
        for (a in vectors.indices) {
            for (b in a + 1 until vectors.size) {
                for (hypothesis in getHypotheses(vectors, a, b, matcher)) {
                    if (hypothesis.isBetterThan(best)) {
                        best = hypothesis
                    }
                }
            }
        }
        return best
    }

    private fun getHypotheses(
        vectors: List<Vector3Precise>,
        a: Int,
        b: Int,
        matcher: CatalogMatcher
    ): List<CatalogMatcher.Hypothesis> {
        val observed = CatalogMatcher.angle(vectors[a], vectors[b])
        return matcher.getPairs(observed, observed * tolerance + MIN_PAIR_TOLERANCE).flatMap { pair ->
            matcher.getHypotheses(vectors, a, b, pair)
        }
    }

    private fun refine(
        readings: List<CelestialObservation>,
        vectors: List<Vector3Precise>,
        matcher: CatalogMatcher,
        initialRotation: Rotation,
        inlierTolerance: Double
    ): List<DetectedStar> {
        var matches = matcher.associate(vectors, initialRotation)
        repeat(REFINE_ITERATIONS) {
            if (matches.size >= 2) {
                matches = matcher.refine(vectors, matches, MIN_ADAPTIVE_TOLERANCE).second
            }
        }
        if (matches.size < minMatches.coerceAtMost(readings.size)) return emptyList()
        return matches.map { match ->
            val star = checkNotNull(matcher.catalog[match.catalogIndex].body.star)
            DetectedStar(star, readings[match.index], (1 - match.distance / inlierTolerance).toFloat().coerceIn(0f, 1f))
        }
    }

    companion object {
        private const val MIN_PAIR_TOLERANCE = 0.25
        private const val INLIER_DEGREES_PER_TOLERANCE = 25.0
        private const val MIN_INLIER_TOLERANCE = 0.5
        const val DEFAULT_MAX_ZENITH_OFFSET = 30.0
        const val UNKNOWN_LOCATION_ZENITH_OFFSET = 180.0
        private const val REFINE_ITERATIONS = 3
        private const val MIN_ADAPTIVE_TOLERANCE = 0.1
    }
}
