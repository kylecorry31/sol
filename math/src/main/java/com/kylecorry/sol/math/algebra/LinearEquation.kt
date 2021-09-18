package com.kylecorry.sol.math.algebra

/**
 * An equation of form y = mx + b
 */
data class LinearEquation(val m: Float, val b: Float) : SingleVariableEquation {
    override fun evaluate(x: Float): Float {
        return m * x + b
    }
}