package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.Polynomial
import com.kylecorry.sol.math.algebra.PolynomialTerm

class NewtonPolynomialRegression(private val order: Int) : PolynomialRegression {
    override fun fit(points: List<Vector2>): Polynomial {
        val sortedPoints = points.sortedBy { it.x }
        val coefficients = getDividedDifferenceCoefficients(sortedPoints)
        val terms = getPolynomialTerms(coefficients, sortedPoints)
        return Polynomial(terms)
    }

    private fun getPolynomialTerms(coefficients: Array<Float>, points: List<Vector2>): List<PolynomialTerm> {
        val n = coefficients.size
        var polynomial = Polynomial.fromCoefficients(coefficients[n - 1])
        for (i in n - 2 downTo 0) {
            polynomial = Polynomial.fromCoefficients(coefficients[i]) + polynomial * Polynomial(
                listOf(
                    PolynomialTerm(1f, 1),
                    PolynomialTerm(-points[i].x, 0)
                )
            )
        }
        return polynomial.terms
    }

    private fun getDividedDifferenceCoefficients(
        points: List<Vector2>
    ): Array<Float> {
        val n = order + 1
        val a = Array(n) { 0f }
        for (i in 0 until n) {
            a[i] = points[i].y
        }
        for (j in 1 until n) {
            for (i in n - 1 downTo j) {
                a[i] = (a[i] - a[i - 1]) / (points[i].x - points[i - j].x)
            }
        }
        return a
    }
}