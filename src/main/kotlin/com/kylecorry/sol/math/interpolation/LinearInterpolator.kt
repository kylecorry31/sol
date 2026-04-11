package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2

class LinearInterpolator(points: List<Vector2>) : Interpolator {
    private val sortedPoints = points.sortedBy { it.x }
    override fun interpolate(x: Float): Float {
        if (sortedPoints.isEmpty()) {
            return 0f
        }

        var startIndex = sortedPoints.indexOfLast { it.x <= x }
        var endIndex = startIndex + 1

        if (startIndex < 0) {
            startIndex = 0
            endIndex = 1
        }

        if (endIndex >= sortedPoints.size) {
            endIndex = sortedPoints.size - 1
            startIndex = endIndex - 1
        }

        val startPoint = sortedPoints[startIndex]
        val endPoint = sortedPoints[endIndex]
        return Interpolation.linear(x, startPoint.x, startPoint.y, endPoint.x, endPoint.y)
    }
}