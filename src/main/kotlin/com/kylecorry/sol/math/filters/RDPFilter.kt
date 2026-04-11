package com.kylecorry.sol.math.filters

import kotlin.math.abs

/**
 * The Ramer–Douglas–Peucker path simplification algorithm
 * Adapted from https://www.youtube.com/watch?v=nSYw9GrakjY
 */
class RDPFilter<T>(
    private val epsilon: Float,
    private val crossTrackDistance: (point: T, start: T, end: T) -> Float,
) {
    fun filter(points: List<T>): List<T> {
        if (points.size < 2) {
            return emptyList()
        }

        val size = points.size
        val lastIndex = size - 1
        val keep = BooleanArray(size)
        keep[0] = true
        keep[lastIndex] = true

        var keptCount = 2

        // Store pending segments as packed start/end indices to avoid Pair allocations.
        val pendingSegments = IntArray(size * 2)
        var pendingSize = 0
        pendingSegments[pendingSize++] = 0
        pendingSegments[pendingSize++] = lastIndex

        while (pendingSize > 0) {
            val endIndex = pendingSegments[--pendingSize]
            val startIndex = pendingSegments[--pendingSize]
            val nextIndex = findFurthest(startIndex, endIndex, points)
            if (nextIndex != -1) {
                if (!keep[nextIndex]) {
                    keep[nextIndex] = true
                    keptCount++
                }
                if (nextIndex + 1 < endIndex) {
                    pendingSegments[pendingSize++] = nextIndex
                    pendingSegments[pendingSize++] = endIndex
                }
                if (startIndex + 1 < nextIndex) {
                    pendingSegments[pendingSize++] = startIndex
                    pendingSegments[pendingSize++] = nextIndex
                }
            }
        }

        val filtered = ArrayList<T>(keptCount)
        for (i in 0 until size) {
            if (keep[i]) {
                filtered.add(points[i])
            }
        }

        return filtered
    }

    private fun findFurthest(
        startIndex: Int,
        endIndex: Int,
        allPoints: List<T>,
    ): Int {
        if (endIndex - startIndex < 2) {
            return -1
        }

        var maxDistance = epsilon
        var maxIndex = -1
        val start = allPoints[startIndex]
        val end = allPoints[endIndex]

        for (i in (startIndex + 1)..<endIndex) {
            val distance = abs(crossTrackDistance(allPoints[i], start, end))
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        return maxIndex
    }
}
