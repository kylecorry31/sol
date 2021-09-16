package com.kylecorry.sol.math.algebra

import kotlin.math.sqrt

class AlgebraService {

    /**
     * Evaluates an equation of form mx + b = y
     */
    fun evaluateLinear(x: Float, m: Float, b: Float): Float {
        return m * x + b
    }

    /**
     * Evaluates an equation of form ax^2 + bx + c = y
     */
    fun evaluateQuadratic(x: Float, a: Float, b: Float, c: Float): Float {
        return a * x * x + b * x + c
    }


    /**
     * Solves an equation of form mx + b = 0
     */
    fun solveLinear(m: Float, b: Float): Float? {
        if (m == 0f) {
            if (b == 0f) {
                return 0f
            }
            return null
        }
        return -b / m
    }

    /**
     * Solves an equation of form ax^2 + bx + c = 0
     */
    fun solveQuadratic(a: Float, b: Float, c: Float): Pair<Float, Float>? {
        val discriminant = b * b - 4 * a * c
        if (discriminant < 0) {
            return null
        }
        val sqrtDiscriminant = sqrt(discriminant)
        val x1 = (-b + sqrtDiscriminant) / (2 * a)
        val x2 = (-b - sqrtDiscriminant) / (2 * a)
        return x1 to x2
    }

}