package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.geometry.Rotation
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Matches observed directions to catalog bodies. A pair of observations and a catalog pair of the same angular
 * separation give a rotation from the observation frame to the sky (TRIAD), and the rotation that lines up the most
 * observations wins. Only the angles between observations are used, so the observation frame's heading doesn't
 * matter.
 * @param up the observer's up direction in the observation frame, or null if unknown
 * @param maxZenithOffset rotations that put up further than this (degrees) from the catalog's zenith are rejected
 * @param inlierTolerance the maximum distance (degrees) between an observation and a body to count as a match
 */
internal class CatalogMatcher(
    val catalog: List<BodyPosition>,
    private val up: Vector3Precise?,
    private val maxZenithOffset: Double,
    private val inlierTolerance: Double
) {

    class Hypothesis(val rotation: Rotation, val score: Int, val residual: Double) {
        fun isBetterThan(other: Hypothesis?): Boolean {
            if (other == null) return true
            return score > other.score || (score == other.score && residual < other.residual)
        }
    }

    data class Match(val index: Int, val catalogIndex: Int, val distance: Double)

    private val pairs = CatalogPairs(catalog, MIN_SEPARATION, MAX_SEPARATION)

    fun getPairs(separation: Double, tolerance: Double): List<CatalogPair> {
        if (separation < MIN_SEPARATION) return emptyList()
        return pairs.near(separation, tolerance)
    }

    /**
     * The hypotheses (both orientations) where observations a and b are the catalog pair
     */
    fun getHypotheses(vectors: List<Vector3Precise>, a: Int, b: Int, pair: CatalogPair): List<Hypothesis> {
        return listOf(pair.first to pair.second, pair.second to pair.first).mapNotNull { (from, to) ->
            Rotation.triad(vectors[a], vectors[b], catalog[from].vector, catalog[to].vector)
                ?.takeIf { isConsistentWithUp(it) }
                ?.let { score(vectors, it) }
        }
    }

    /**
     * @return the closest body to each observation within the tolerance, each body is matched once
     */
    fun associate(
        vectors: List<Vector3Precise>,
        rotation: Rotation,
        tolerance: Double = inlierTolerance
    ): List<Match> {
        return vectors.mapIndexedNotNull { index, vector ->
            val (nearest, distance) = CatalogPairs.nearest(rotation.rotate(vector), catalog)
            if (nearest >= 0 && distance <= tolerance) Match(index, nearest, distance) else null
        }.groupBy { it.catalogIndex }.map { group -> group.value.minBy { it.distance } }
    }

    fun fitRotation(vectors: List<Vector3Precise>, matches: List<Match>): Rotation {
        return Rotation.fit(matches.map { vectors[it.index] }, matches.map { catalog[it.catalogIndex].vector })
    }

    fun rms(vectors: List<Vector3Precise>, matches: List<Match>, rotation: Rotation): Double {
        return rms(matches.map { vectors[it.index] }, matches.map { catalog[it.catalogIndex].vector }, rotation)
    }

    /**
     * Fits the rotation to the matches and re-associates the observations. The tolerance shrinks with the fit
     * error so faint observations don't match a nearby catalog body.
     */
    fun refine(
        vectors: List<Vector3Precise>,
        matches: List<Match>,
        minTolerance: Double
    ): Pair<Rotation, List<Match>> {
        val rotation = fitRotation(vectors, matches)
        val tolerance = (rms(vectors, matches, rotation) * INLIER_RMS_SCALE).coerceIn(minTolerance, inlierTolerance)
        return rotation to associate(vectors, rotation, tolerance)
    }

    private fun isConsistentWithUp(rotation: Rotation): Boolean {
        val up = up ?: return true
        return angle(rotation.rotate(up), HorizonVectors.up) <= maxZenithOffset
    }

    private fun score(vectors: List<Vector3Precise>, rotation: Rotation): Hypothesis {
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

    companion object {
        private const val MIN_SEPARATION = 1.0
        private const val MAX_SEPARATION = 130.0
        private const val INLIER_RMS_SCALE = 4.0

        fun angle(a: Vector3Precise, b: Vector3Precise): Double {
            return Math.toDegrees(acos((a.dot(b) / (a.magnitude() * b.magnitude())).coerceIn(-1.0, 1.0)))
        }

        fun rms(rays: List<Vector3Precise>, targets: List<Vector3Precise>, rotation: Rotation): Double {
            if (rays.isEmpty()) return 0.0
            val sum = rays.indices.sumOf {
                val d = angle(rotation.rotate(rays[it]), targets[it])
                d * d
            }
            return sqrt(sum / rays.size)
        }
    }
}
