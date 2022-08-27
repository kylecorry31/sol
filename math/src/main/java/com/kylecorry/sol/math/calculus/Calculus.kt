package com.kylecorry.sol.math.calculus

import com.kylecorry.sol.math.algebra.LinearEquation
import com.kylecorry.sol.math.algebra.QuadraticEquation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Calculus {

    fun derivative(equation: QuadraticEquation): LinearEquation {
        return LinearEquation(equation.a * 2, equation.b)
    }

    fun derivative(equation: LinearEquation): Float {
        return equation.m
    }

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
        if (x < end) {
            val endValue = fn(end)
            total += (end - x) * (startValue + endValue) / 2
        }

        return multiplier * total
    }

    /**
     * Calculate the root of the provided function using Newton's Method
     */
    fun root(
        fn: (x: Double) -> Double,
        fnPrime: (x: Double) -> Double = { derivative(it, fn = fn) },
        guess: Double = 0.0,
        maxIterations: Int = 5,
        threshold: Double = 0.0
    ): Double {
        var x = guess
        for (i in 0 until maxIterations) {
            val delta = fn(x) / fnPrime(x)
            x -= delta
            if (abs(delta) < threshold) break
        }
        return x
    }


}