package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2

/**
 * A newton interpolator that only uses the points surrounding the x to interpolate rather than the points from the beginning.
 */
class LocalNewtonInterpolator(points: List<Vector2>, private val order: Int) : Interpolator {
    private val sortedPoints = points.sortedBy { it.x }
    private var cachedPoints = emptyList<Vector2>()
    private var cachedInterpolator: NewtonInterpolator? = null

    private val lock = Any()

    override fun interpolate(x: Float): Float {
        if (sortedPoints.isEmpty()) {
            return 0f
        }

        val lastBeforeIndex = sortedPoints.indexOfLast { it.x <= x }
        var startIndex = lastBeforeIndex - order + 1
        var endIndex = startIndex + order

        if (startIndex < 0) {
            startIndex = 0
            endIndex = order
        }

        if (endIndex >= sortedPoints.size) {
            endIndex = sortedPoints.size - 1
            startIndex = (endIndex - order).coerceAtLeast(0)
        }

        if ((endIndex - startIndex) < order) {
            val linear = LinearInterpolator(sortedPoints)
            return linear.interpolate(x)
        }

        val points = sortedPoints.subList(startIndex, endIndex + 1)
        return interpolate(x, points)
    }


    private fun interpolate(
        x: Float,
        points: List<Vector2>
    ): Float {
        synchronized(lock) {
            if (points != cachedPoints || cachedInterpolator == null) {
                cachedPoints = points.toList()
                cachedInterpolator = NewtonInterpolator(points, order)
            }

            return cachedInterpolator?.interpolate(x) ?: 0f
        }
    }
}