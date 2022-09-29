package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.LinearEquation

class LinearRegression(data: List<Vector2>) : IRegression1D {

    val equation = fit(data)

    override fun predict(x: Float): Float {
        return equation.evaluate(x)
    }

    private fun fit(data: List<Vector2>): LinearEquation {
        val coefs = LeastSquaresRegression(
            data.map { listOf(it.x) },
            data.map { it.y }
        ).coefs

        return LinearEquation(coefs[0], coefs[1])
    }

}