package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.regression.NewtonPolynomialRegression

class NewtonInterpolator(points: List<Vector2>, order: Int = points.size - 1) : Interpolator {
    private val regression = NewtonPolynomialRegression(order)
    private var polynomial = regression.fit(points)

    override fun interpolate(x: Float): Float {
        return polynomial.evaluate(x)
    }
}