package com.kylecorry.sol.math.interpolation

class NewtonInterpolator {

    private var cachedXs = emptyList<Float>()
    private var cachedYs = emptyList<Float>()
    private var cachedA = emptyArray<Float>()

    private val lock = Any()

    /**
     * Use Newton's divided difference to interpolate a value. Requires order + 1 points.
     * @param x: The x value to interpolate
     * @param xs: The x values of the control points
     * @param ys: The y values of the control points
     * @return The y value of the interpolation
     */
    fun interpolate(
        x: Float,
        xs: List<Float>,
        ys: List<Float>,
        order: Int = xs.size - 1
    ): Float {
        synchronized(lock) {

            // If the xs, ys, or order changed, recalculate the coefficients
            if (xs != cachedXs || ys != cachedYs || order != cachedA.size - 1) {
                cachedXs = xs.toList()
                cachedYs = ys.toList()
                cachedA = getDividedDifferenceCoefficients(xs, ys, order)
            }

            return dividedDifferencePrecomputed(x, xs, cachedA)
        }
    }

    private fun getDividedDifferenceCoefficients(
        xs: List<Float>,
        ys: List<Float>,
        order: Int = xs.size - 1
    ): Array<Float> {
        val n = order + 1
        val a = Array(n) { 0f }
        for (i in 0 until n) {
            a[i] = ys[i]
        }
        for (j in 1 until n) {
            for (i in n - 1 downTo j) {
                a[i] = (a[i] - a[i - 1]) / (xs[i] - xs[i - j])
            }
        }
        return a
    }


    private fun dividedDifferencePrecomputed(x: Float, xs: List<Float>, a: Array<Float>): Float {
        val n = a.size
        var y = a[n - 1]
        for (i in n - 2 downTo 0) {
            y = a[i] + (x - xs[i]) * y
        }
        return y
    }

}