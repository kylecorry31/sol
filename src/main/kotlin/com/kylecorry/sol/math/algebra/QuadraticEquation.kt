package com.kylecorry.sol.math.algebra

import com.kylecorry.sol.math.arithmetic.Arithmetic.square

/**
 * An equation of form y = ax^2 + bx + c
 */
data class QuadraticEquation(val a: Float, val b: Float, val c: Float) : SingleVariableEquation {
    override fun evaluate(x: Float): Float {
        return a * square(x) + b * x + c
    }
}
