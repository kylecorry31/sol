package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range

class ConvergenceOptimizer(
    private val initialStep: Float,
    private val precision: Float,
    private val initialValue: Pair<Double, Double>? = null,
    private val getOptimizer: (step: Float, initialValue: Pair<Double, Double>?) -> IOptimizer
) : IOptimizer {
    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (Double, Double) -> Double
    ): Pair<Double, Double> {

        var step = initialStep
        var currentXRange = xRange
        var currentYRange = yRange
        var center = initialValue ?: ((xRange.start + xRange.end) / 2 to (yRange.start + yRange.end) / 2)

        while (step > precision) {
            val optimizer = getOptimizer(step, center)
            val (x, y) = optimizer.optimize(currentXRange, currentYRange, maximize, fn)
            var xSize = (currentXRange.end - currentXRange.start) / 2
            var ySize = (currentYRange.end - currentYRange.start) / 2

            var startX = (x - xSize).coerceAtLeast(xRange.start)
            var endX = (x + xSize).coerceAtMost(xRange.end)
            var startY = (y - ySize).coerceAtLeast(yRange.start)
            var endY = (y + ySize).coerceAtMost(yRange.end)

            currentXRange = Range(startX, endX)
            currentYRange = Range(startY, endY)
            center = x to y

            step /= 2
        }

        return center
    }


}