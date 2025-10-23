package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.Polynomial
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class NewtonPolynomialRegressionTest {

    @ParameterizedTest
    @CsvSource(
        "'2x + 1', '2x + 1', 1",
        "'x^2 + 2x + 1', 'x^2 + 2x + 1', 2",
        "'x^3 - 2x^2 + 3x - 4', 'x^3 - 2x^2 + 3x - 4', 3",
        "'5', '5', 1",
        "'x^2', 'x^2', 2",
        "'2x - 3', '2x - 3', 1",
        "'0.5x', '0.5x', 1",
        "'x^4', 'x^4', 4",
        "'x', 'x', 1",
        "'x^3 + 2x^2 - x + 3', 'x^3 + 2x^2 - x + 3', 3",
        "'x + 1', 'x + 1', 1",
        "'3x^2 - 2x + 7', '3x^2 - 2x + 7', 2",
        "'x^3', 'x^3', 3",
        "'4x - 5', '4x - 5', 1",
        // Lower order than required, it will approximate
        "'x^2 + 2x + 1', '3x + 1', 1"
    )
    fun testFit(sourcePolynomial: String, expectedPolynomial: String, order: Int) {
        val sourcePoly = Polynomial.of(sourcePolynomial)
        val points = (0..10).map { x -> Vector2(x.toFloat(), sourcePoly.evaluate(x.toFloat())) }

        val regression = NewtonPolynomialRegression(order)
        val polynomial = regression.fit(points)

        assertEquals(Polynomial.of(expectedPolynomial), polynomial)
    }
}
