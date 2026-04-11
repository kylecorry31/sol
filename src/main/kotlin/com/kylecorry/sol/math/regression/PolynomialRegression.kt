package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.Polynomial

interface PolynomialRegression {
    fun fit(points: List<Vector2>): Polynomial
}