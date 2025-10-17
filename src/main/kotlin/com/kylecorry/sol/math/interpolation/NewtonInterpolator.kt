package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2

class NewtonInterpolator(points: List<Vector2>, private val order: Int = points.size - 1) : Interpolator {

    private var sortedPoints = points.sortedBy { it.x }
    private var cachedA = emptyArray<Float>()

    private val lock = Any()

    override fun interpolate(x: Float): Float {
        synchronized(lock) {
            if (cachedA.isEmpty()) {
                cachedA = getDividedDifferenceCoefficients(sortedPoints)
            }

            return dividedDifferencePrecomputed(x, sortedPoints, cachedA)
        }
    }

    private fun getDividedDifferenceCoefficients(
        points: List<Vector2>
    ): Array<Float> {
        val n = order + 1
        val a = Array(n) { 0f }
        for (i in 0 until n) {
            a[i] = points[i].y
        }
        for (j in 1 until n) {
            for (i in n - 1 downTo j) {
                a[i] = (a[i] - a[i - 1]) / (points[i].x - points[i - j].x)
            }
        }
        return a
    }


    private fun dividedDifferencePrecomputed(x: Float, points: List<Vector2>, a: Array<Float>): Float {
        val n = a.size
        var y = a[n - 1]
        for (i in n - 2 downTo 0) {
            y = a[i] + (x - points[i].x) * y
        }
        return y
    }

}