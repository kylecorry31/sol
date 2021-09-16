package com.kylecorry.sol.math.calculus

import kotlin.math.max
import kotlin.math.min

class CalculusService {

    fun derivative(
        x: Double,
        step: Double = 0.0001,
        fn: (x: Double) -> Double
    ): Double {
        val current = fn(x)
        return (fn(x + step) - current) / step
    }

    fun derivative(
        x: Double,
        y: Double,
        step: Double = 0.0001,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double> {
        val current = fn(x, y)
        val xGrad = (fn(x + step, y) - current) / step
        val yGrad = (fn(x, y + step) - current) / step
        return xGrad to yGrad
    }

    fun integral(
        startX: Double,
        endX: Double,
        step: Double = 0.0001,
        fn: (x: Double) -> Double
    ): Double {

        val start = min(startX, endX)
        val end = max(startX, endX)

        val multiplier = if (endX < startX) {
            -1
        } else {
            1
        }

        if (end - start < step || step <= 0.0) {
            return multiplier * (end - start) * (fn(start) + fn(end)) / 2
        }

        var total = 0.0
        var x = start
        var startValue = fn(x)
        while (x < end) {
            val endValue = fn(x + step)
            total += step * (startValue + endValue) / 2
            startValue = endValue
            x += step
        }

        // Add up the last piece
        if (x < end){
            val endValue = fn(end)
            total += (end - x) * (startValue + endValue) / 2
        }

        return multiplier * total
    }


}