package com.kylecorry.sol.math.algebra

import com.kylecorry.sol.math.arithmetic.Arithmetic
import kotlin.math.sqrt

object Algebra {

    /**
     * Solves an equation of form mx + b = 0
     */
    fun solve(equation: LinearEquation): Float? {
        if (Arithmetic.isZero(equation.m)) {
            if (Arithmetic.isZero(equation.b)) {
                return 0f
            }
            return null
        }
        return -equation.b / equation.m
    }

    fun solve(equation: QuadraticEquation): Pair<Float, Float>? {
        if (Arithmetic.isZero(equation.a)) {
            val linear = solve(LinearEquation(equation.b, equation.c)) ?: return null
            return linear to linear
        }

        val discriminant = equation.b * equation.b - 4 * equation.a * equation.c
        if (discriminant < 0) {
            return null
        }
        val sqrtDiscriminant = sqrt(discriminant)
        val x1 = (-equation.b + sqrtDiscriminant) / (2 * equation.a)
        val x2 = (-equation.b - sqrtDiscriminant) / (2 * equation.a)
        return x1 to x2
    }

    fun inverse(equation: LinearEquation): LinearEquation {
        return LinearEquation(1 / equation.m, -equation.b / equation.m)
    }

    /**
     * Computes a polynomial
     * Ex. 1 + 2x + 5x^2 + x^4
     * polynomial(x, 1, 2, 5, 0, 1)
     */
    fun polynomial(x: Double, vararg coefs: Double): Double {
        var runningTotal = 0.0
        var xPower = 1.0
        for (i in coefs.indices) {
            runningTotal += xPower * coefs[i]
            xPower *= x
        }

        return runningTotal
    }

}