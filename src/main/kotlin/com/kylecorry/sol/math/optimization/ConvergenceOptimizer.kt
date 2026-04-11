package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import kotlin.math.ceil
import kotlin.math.ln

class ConvergenceOptimizer(
    private val initialStep: Float,
    private val precision: Float,
    private val initialValue: Pair<Double, Double>? = null,
    private val getOptimizer: (step: Float, initialValue: Pair<Double, Double>?) -> IOptimizer,
) : IOptimizer {
    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (Double, Double) -> Double,
    ): Pair<Double, Double> {
        require(initialStep.isFinite() && initialStep > 0f) { "Initial step must be finite and greater than 0" }
        require(precision.isFinite() && precision > 0f) { "Precision must be finite and greater than 0" }

        var step = initialStep
        var currentXRange = xRange
        var currentYRange = yRange
        var center = initialValue ?: ((xRange.start + xRange.end) / 2 to (yRange.start + yRange.end) / 2)
        val maxIterations = ceil(ln((initialStep / precision).toDouble()) / ln(2.0)).toInt().coerceAtLeast(0) + 1

        repeat(maxIterations) {
            if (step <= precision) {
                return center
            }
            val optimizer = getOptimizer(step, center)
            val (x, y) = optimizer.optimize(currentXRange, currentYRange, maximize, fn)
            val xSize = (currentXRange.end - currentXRange.start) / 2
            val ySize = (currentYRange.end - currentYRange.start) / 2

            val startX = (x - xSize).coerceAtLeast(xRange.start)
            val endX = (x + xSize).coerceAtMost(xRange.end)
            val startY = (y - ySize).coerceAtLeast(yRange.start)
            val endY = (y + ySize).coerceAtMost(yRange.end)

            currentXRange = Range(startX, endX)
            currentYRange = Range(startY, endY)
            center = x to y

            step /= 2
        }

        return center
    }
}
