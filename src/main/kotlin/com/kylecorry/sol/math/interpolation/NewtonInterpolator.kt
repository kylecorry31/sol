package com.kylecorry.sol.math.interpolation

class NewtonInterpolator {

    private var cachedXs = emptyList<Float>()
    private var cachedYs = emptyList<Float>()
    private var cachedA = emptyArray<Float>()

    private val lock = Any()

    /**
     * Use Newton's divided difference to interpolate a value. Automatically selects the nearest
     * order + 1 points from the dataset around the target x value.
     * @param x: The x value to interpolate
     * @param xs: The x values of the dataset (should be sorted)
     * @param ys: The y values of the dataset
     * @param order: The order of the interpolation (default is 3 for cubic interpolation)
     * @return The y value of the interpolation
     */
    fun interpolate(
        x: Float,
        xs: List<Float>,
        ys: List<Float>,
        order: Int = 3
    ): Float {
        require(xs.size == ys.size) { "xs and ys must have the same size" }
        require(order >= 0) { "order must be non-negative" }
        require(xs.size > order) { "dataset must have at least order + 1 points" }

        // Select the nearest order + 1 points around x
        val selectedIndices = selectNearestPoints(x, xs, order + 1)
        val selectedXs = selectedIndices.map { xs[it] }
        val selectedYs = selectedIndices.map { ys[it] }

        return interpolateWithPoints(x, selectedXs, selectedYs, order)
    }

    /**
     * Use Newton's divided difference to interpolate a value. Requires exactly order + 1 points.
     * For best results, provide the nearest points around the x value to interpolate.
     * @param x: The x value to interpolate
     * @param xs: The x values of the control points
     * @param ys: The y values of the control points
     * @param order: The order of the interpolation (default is xs.size - 1)
     * @return The y value of the interpolation
     */
    fun interpolateWithPoints(
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

    /**
     * Select the nearest n points around x from the dataset.
     * Tries to get n/2 points on each side of x, but adjusts if near the edges.
     * @param x: The x value to find nearest points for
     * @param xs: The sorted x values of the dataset
     * @param n: The number of points to select
     * @return The indices of the selected points
     */
    private fun selectNearestPoints(x: Float, xs: List<Float>, n: Int): List<Int> {
        if (n >= xs.size) {
            return xs.indices.toList()
        }

        // Find the index where x would be inserted to keep the list sorted
        val insertIndex = xs.binarySearch(x).let { if (it < 0) -(it + 1) else it }

        // Calculate how many points to take on each side
        val halfN = n / 2
        var startIndex = insertIndex - halfN
        var endIndex = startIndex + n

        // Adjust if we're near the start
        if (startIndex < 0) {
            startIndex = 0
            endIndex = n
        }

        // Adjust if we're near the end
        if (endIndex > xs.size) {
            endIndex = xs.size
            startIndex = xs.size - n
        }

        return (startIndex until endIndex).toList()
    }

}