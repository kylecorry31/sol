package com.kylecorry.sol.math.filters

/**
 * The Ramer–Douglas–Peucker path simplification algorithm
 * Adapted from https://www.youtube.com/watch?v=nSYw9GrakjY
 */
class RDPFilter<T>(
    private val epsilon: Float,
    private val crossTrackDistance: (point: T, start: T, end: T) -> Float
) {

    fun filter(points: List<T>): List<T> {
        if (points.size < 2) {
            return emptyList()
        }

        val rdp = mutableListOf<T>()

        rdp.add(points.first())
        filterHelper(0, points.lastIndex, points, rdp)
        rdp.add(points.last())

        return rdp
    }

    private fun filterHelper(
        startIndex: Int,
        endIndex: Int,
        allPoints: List<T>,
        rdpPoints: MutableList<T>
    ) {
        val nextIndex = findFurthest(startIndex, endIndex, allPoints)
        if (nextIndex > 0) {
            if (startIndex != nextIndex) {
                filterHelper(startIndex, nextIndex, allPoints, rdpPoints)
            }
            rdpPoints.add((allPoints[nextIndex]))
            if (endIndex != nextIndex) {
                filterHelper(nextIndex, endIndex, allPoints, rdpPoints)
            }
        }
    }

    private fun findFurthest(startIndex: Int, endIndex: Int, allPoints: List<T>): Int {
        var maxDistance = Float.NEGATIVE_INFINITY
        var maxIndex = -1
        val start = allPoints[startIndex]
        val end = allPoints[endIndex]

        for (i in (startIndex + 1) until endIndex) {
            val distance = crossTrackDistance(allPoints[i], start, end)
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        return if (maxDistance > epsilon) maxIndex else -1
    }

}