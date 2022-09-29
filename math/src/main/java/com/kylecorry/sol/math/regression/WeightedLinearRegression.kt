package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.*

class WeightedLinearRegression(data: List<Vector2>, weights: List<Float>) : IRegression1D {

    val equation = fit(data, weights)

    override fun predict(x: Float): Float {
        return equation.evaluate(x)
    }

    private fun fit(data: List<Vector2>, weights: List<Float>): LinearEquation {
        val coefs = WeightedLeastSquaresRegression(
            data.map { listOf(it.x) },
            data.map { it.y },
            weights
        ).coefs

        return LinearEquation(coefs[0], coefs[1])
    }
}