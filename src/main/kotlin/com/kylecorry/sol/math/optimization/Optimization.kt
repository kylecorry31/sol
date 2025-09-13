package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.SolMath
import kotlin.math.absoluteValue

object Optimization {

    inline fun newtonRaphsonIteration(
        initialValue: Double = 0.0,
        tolerance: Double = SolMath.EPSILON_DOUBLE,
        maxIterations: Int = Int.MAX_VALUE,
        crossinline calculate: (lastValue: Double) -> Double
    ): Double {
        var lastValue = initialValue
        var iterations = 0
        var delta: Double
        do {
            val newValue = calculate(initialValue)
            delta = newValue - lastValue
            lastValue = newValue
            iterations++
        } while (iterations < maxIterations && delta.absoluteValue > tolerance)
        return lastValue
    }

    inline fun newtonRaphsonIteration(
        initialValue: Float = 0f,
        tolerance: Float = SolMath.EPSILON_FLOAT,
        maxIterations: Int = Int.MAX_VALUE,
        crossinline calculate: (lastValue: Float) -> Float
    ): Float {
        return newtonRaphsonIteration(initialValue.toDouble(), tolerance.toDouble(), maxIterations) {
            calculate(it.toFloat()).toDouble()
        }.toFloat()
    }

}