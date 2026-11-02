package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import kotlin.math.acos

internal data class CatalogPair(val separation: Double, val first: Int, val second: Int)

/**
 * Every pair of catalog bodies sorted by angular separation, for finding pairs that match an
 * observed separation
 */
internal class CatalogPairs(val catalog: List<BodyPosition>, minSeparation: Double, maxSeparation: Double) {
    private val pairs: List<CatalogPair>
    private val separations: DoubleArray

    init {
        val all = mutableListOf<CatalogPair>()
        for (i in catalog.indices) {
            for (j in i + 1 until catalog.size) {
                val d = CatalogMatcher.angle(catalog[i].vector, catalog[j].vector)
                if (d in minSeparation..maxSeparation) all.add(CatalogPair(d, i, j))
            }
        }
        pairs = all.sortedBy { it.separation }
        separations = pairs.map { it.separation }.toDoubleArray()
    }

    fun near(separation: Double, tolerance: Double): List<CatalogPair> {
        val start = lowerBound(separations, separation - tolerance)
        val end = lowerBound(separations, separation + tolerance)
        return pairs.subList(start, end.coerceAtLeast(start))
    }

    companion object {
        /**
         * @return the index of the closest body and its distance in degrees
         */
        fun nearest(vector: Vector3Precise, catalog: List<BodyPosition>): Pair<Int, Double> {
            var bestIndex = -1
            var bestDot = -2.0
            for (i in catalog.indices) {
                val d = vector.dot(catalog[i].vector)
                if (d > bestDot) {
                    bestDot = d
                    bestIndex = i
                }
            }
            return bestIndex to Math.toDegrees(acos(bestDot.coerceIn(-1.0, 1.0)))
        }

        private fun lowerBound(values: DoubleArray, target: Double): Int {
            var lo = 0
            var hi = values.size
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (values[mid] < target) lo = mid + 1 else hi = mid
            }
            return lo
        }
    }
}
