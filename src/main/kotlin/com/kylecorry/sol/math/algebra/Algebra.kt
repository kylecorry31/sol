package com.kylecorry.sol.math.algebra

import com.kylecorry.sol.math.SolMath
import kotlin.math.sqrt

object Algebra {

    /**
     * Solves an equation of form mx + b = 0
     */
    fun solve(equation: LinearEquation): Float? {
        if (SolMath.isZero(equation.m)) {
            if (SolMath.isZero(equation.b)) {
                return 0f
            }
            return null
        }
        return -equation.b / equation.m
    }

    fun solve(equation: QuadraticEquation): Pair<Float, Float>? {
        if (SolMath.isZero(equation.a)) {
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

}