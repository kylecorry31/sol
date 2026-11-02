package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.astronomy.navigation.BodyPosition
import com.kylecorry.sol.science.astronomy.navigation.CatalogPairs
import com.kylecorry.sol.science.astronomy.navigation.HorizonVectors
import com.kylecorry.sol.science.astronomy.navigation.SkyCatalog
import com.kylecorry.sol.science.astronomy.navigation.StarIdentifier
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.units.Coordinate
import java.time.ZonedDateTime

/**
 * Identifies stars from altitude / azimuth readings. Every pair of readings is compared with
 * catalog pairs of the same angular separation, each candidate gives a rotation from the sensor's
 * frame to the sky, and the rotation that lines up the most readings wins. Every identification is
 * geometrically consistent with the others, and a constant compass error doesn't matter.
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

    private class Hypothesis(val rotation: Rotation, val score: Int, val residual: Double)

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
        val best = findBestHypothesis(vectors, catalog, inlierTolerance)
            ?.takeIf { it.score >= minMatches.coerceAtMost(readings.size) }
        return best?.let { refine(readings, vectors, catalog, it.rotation, inlierTolerance) }.orEmpty()
    }

    private fun findBestHypothesis(
        vectors: List<Vector3Precise>,
        catalog: List<BodyPosition>,
        inlierTolerance: Double
    ): Hypothesis? {
        val pairs = CatalogPairs(catalog, MIN_SEPARATION, MAX_SEPARATION)
        var best: Hypothesis? = null
        for (a in vectors.indices) {
            for (b in a + 1 until vectors.size) {
                for (hypothesis in getHypotheses(vectors, a, b, pairs, inlierTolerance)) {
                    if (isBetter(hypothesis, best)) {
                        best = hypothesis
                    }
                }
            }
        }
        return best
    }

    /**
     * All hypotheses where readings a and b are a catalog pair of the same separation
     */
    private fun getHypotheses(
        vectors: List<Vector3Precise>,
        a: Int,
        b: Int,
        pairs: CatalogPairs,
        inlierTolerance: Double
    ): List<Hypothesis> {
        val observed = StarIdentifier.angle(vectors[a], vectors[b])
        if (observed < MIN_SEPARATION) return emptyList()
        return pairs.near(observed, observed * tolerance + MIN_PAIR_TOLERANCE).flatMap { pair ->
            listOf(pair.first to pair.second, pair.second to pair.first).mapNotNull { (from, to) ->
                createHypothesis(vectors, a, b, pairs.catalog[from], pairs.catalog[to], pairs.catalog, inlierTolerance)
            }
        }
    }

    @Suppress("LongParameterList")
    private fun createHypothesis(
        vectors: List<Vector3Precise>,
        a: Int,
        b: Int,
        bodyA: BodyPosition,
        bodyB: BodyPosition,
        catalog: List<BodyPosition>,
        inlierTolerance: Double
    ): Hypothesis? {
        // The readings' up is the zenith at the true location, which must be near the approximate one
        val rotation = Rotation.triad(vectors[a], vectors[b], bodyA.vector, bodyB.vector)
            ?.takeIf { StarIdentifier.angle(it.rotate(HorizonVectors.up), HorizonVectors.up) <= maxZenithOffset }
            ?: return null
        var score = 0
        var residual = 0.0
        for (vector in vectors) {
            val distance = CatalogPairs.nearest(rotation.rotate(vector), catalog).second
            if (distance <= inlierTolerance) {
                score++
                residual += distance
            }
        }
        return Hypothesis(rotation, score, residual)
    }

    private fun isBetter(candidate: Hypothesis, current: Hypothesis?): Boolean {
        if (current == null) return true
        return candidate.score > current.score ||
                (candidate.score == current.score && candidate.residual < current.residual)
    }

    private fun refine(
        readings: List<CelestialObservation>,
        vectors: List<Vector3Precise>,
        catalog: List<BodyPosition>,
        initialRotation: Rotation,
        inlierTolerance: Double
    ): List<DetectedStar> {
        var rotation = initialRotation
        var tolerance = inlierTolerance
        var matches = associate(vectors, catalog, rotation, tolerance)
        repeat(REFINE_ITERATIONS) {
            if (matches.size >= 2) {
                val from = matches.map { match -> vectors[match.first] }
                val to = matches.map { match -> catalog[match.second].vector }
                rotation = Rotation.fit(from, to)
                // Readings fainter than the catalog would otherwise match a nearby catalog star
                tolerance = (StarIdentifier.rms(from, to, rotation) * INLIER_RMS_SCALE)
                    .coerceIn(MIN_ADAPTIVE_TOLERANCE, inlierTolerance)
                matches = associate(vectors, catalog, rotation, tolerance)
            }
        }
        if (matches.size < minMatches.coerceAtMost(readings.size)) return emptyList()
        return matches.map { (index, body, distance) ->
            val star = checkNotNull(catalog[body].body.star)
            DetectedStar(star, readings[index], (1 - distance / inlierTolerance).toFloat().coerceIn(0f, 1f))
        }
    }

    /**
     * @return the reading index, catalog index, and distance of each match (each body matched once)
     */
    private fun associate(
        vectors: List<Vector3Precise>,
        catalog: List<BodyPosition>,
        rotation: Rotation,
        inlierTolerance: Double
    ): List<Triple<Int, Int, Double>> {
        return vectors.mapIndexedNotNull { index, vector ->
            val (nearest, distance) = CatalogPairs.nearest(rotation.rotate(vector), catalog)
            if (nearest >= 0 && distance <= inlierTolerance) Triple(index, nearest, distance) else null
        }.groupBy { it.second }.map { group -> group.value.minBy { it.third } }
    }

    companion object {
        private const val MIN_SEPARATION = 1.0
        private const val MAX_SEPARATION = 130.0
        private const val MIN_PAIR_TOLERANCE = 0.25
        private const val INLIER_DEGREES_PER_TOLERANCE = 25.0
        private const val MIN_INLIER_TOLERANCE = 0.5
        const val DEFAULT_MAX_ZENITH_OFFSET = 30.0
        const val UNKNOWN_LOCATION_ZENITH_OFFSET = 180.0
        private const val REFINE_ITERATIONS = 3
        private const val INLIER_RMS_SCALE = 4.0
        private const val MIN_ADAPTIVE_TOLERANCE = 0.1
    }
}
