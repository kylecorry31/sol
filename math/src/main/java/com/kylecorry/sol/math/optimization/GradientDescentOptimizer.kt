package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.lerp
import com.kylecorry.sol.math.calculus.Calculus
import kotlin.math.absoluteValue
import kotlin.random.Random

class GradientDescentOptimizer(
    private val learningRate: Double,
    private val maxIterations: Int = 1000,
    private val gradientThreshold: Double = 0.001,
    private val gradientFn: (x: Double, y: Double) -> Pair<Double, Double>
) : IOptimizer {

    private val random = Random(1)

    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double> {
        var x = lerp(random.nextDouble(), xRange.start, xRange.end)
        var y = lerp(random.nextDouble(), yRange.start, yRange.end)
        var gradient = gradientFn(x, y)
        var i = 0
        while ((gradient.first.absoluteValue > gradientThreshold || gradient.second.absoluteValue > gradientThreshold) && i <= maxIterations) {
            x += (if (maximize) 1 else -1) * learningRate * gradient.first
            y += (if (maximize) 1 else -1) * learningRate * gradient.second
            x = xRange.clamp(x)
            y = yRange.clamp(y)
            gradient = gradientFn(x, y)
            i++
        }

        return x to y
    }

    companion object {
        fun approximateGradientFn(
            step: Double = 0.00001,
            fn: (x: Double, y: Double) -> Double
        ): (x: Double, y: Double) -> Pair<Double, Double> {
            return { x: Double, y: Double ->
                Calculus.derivative(x, y, step, fn)
            }
        }
    }
}