package com.kylecorry.sol.math.algebra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PolynomialTest {

    @Test
    fun evaluate() {
        // Test linear: 2x + 3
        val linear = Polynomial.fromCoefficients(3f, 2f)
        assertEquals(3f, linear.evaluate(0f), 0.001f)
        assertEquals(5f, linear.evaluate(1f), 0.001f)
        assertEquals(7f, linear.evaluate(2f), 0.001f)
        assertEquals(13f, linear.evaluate(5f), 0.001f)

        // Test quadratic: x^2 + 2x + 1
        val quadratic = Polynomial.fromCoefficients(1f, 2f, 1f)
        assertEquals(1f, quadratic.evaluate(0f), 0.001f)
        assertEquals(4f, quadratic.evaluate(1f), 0.001f)
        assertEquals(9f, quadratic.evaluate(2f), 0.001f)
        assertEquals(36f, quadratic.evaluate(5f), 0.001f)

        // Test cubic: x^3 - 2x^2 + 3x - 4
        val cubic = Polynomial.fromCoefficients(-4f, 3f, -2f, 1f)
        assertEquals(-4f, cubic.evaluate(0f), 0.001f)
        assertEquals(-2f, cubic.evaluate(1f), 0.001f)
        assertEquals(2f, cubic.evaluate(2f), 0.001f)

        // Test constant: 5
        val constant = Polynomial.fromCoefficients(5f)
        assertEquals(5f, constant.evaluate(0f), 0.001f)
        assertEquals(5f, constant.evaluate(10f), 0.001f)
        assertEquals(5f, constant.evaluate(-3f), 0.001f)
    }

    @Test
    fun derivative() {
        // Test linear: 2x + 3 -> 2
        val linear = Polynomial.fromCoefficients(3f, 2f)
        assertEquals(Polynomial.of("2"), linear.derivative())

        // Test quadratic: x^2 + 2x + 1 -> 2x + 2
        val quadratic = Polynomial.fromCoefficients(1f, 2f, 1f)
        assertEquals(Polynomial.of("2x + 2"), quadratic.derivative())

        // Test cubic: x^3 - 2x^2 + 3x - 4 -> 3x^2 - 4x + 3
        val cubic = Polynomial.fromCoefficients(-4f, 3f, -2f, 1f)
        assertEquals(Polynomial.of("3x^2 - 4x + 3"), cubic.derivative())

        // Test constant: 5 -> 0 (empty list)
        val constant = Polynomial.fromCoefficients(5f)
        assertEquals(Polynomial.of("0"), constant.derivative())
    }

    @Test
    fun integral() {
        // Test linear: 2x + 3 -> x^2 + 3x + C
        val linear = Polynomial.fromCoefficients(3f, 2f)
        assertEquals(Polynomial.of("x^2 + 3x"), linear.integral())

        // Test quadratic: x^2 + 2x + 1 -> (1/3)x^3 + x^2 + x + C
        val quadratic = Polynomial.fromCoefficients(1f, 2f, 1f)
        assertEquals(Polynomial.of("(1/3)x^3 + x^2 + x"), quadratic.integral())

        // Test cubic: x^3 - 2x^2 + 3x - 4 -> (1/4)x^4 - (2/3)x^3 + (3/2)x^2 - 4x + C
        val cubic = Polynomial.fromCoefficients(-4f, 3f, -2f, 1f)
        assertEquals(Polynomial.of("(1/4)x^4 - (2/3)x^3 + (3/2)x^2 - 4x"), cubic.integral())

        // Test constant: 5 -> 5x + C
        val constant = Polynomial.fromCoefficients(5f)
        assertEquals(Polynomial.of("5x"), constant.integral())
    }

    @Test
    fun ofTerms() {
        val poly = Polynomial.of(
            PolynomialTerm(3f, 0),
            PolynomialTerm(2f, 1),
            PolynomialTerm(1f, 2)
        )
        assertEquals(Polynomial.of("1x^2 + 2x + 3"), poly)
    }

    @Test
    fun fromCoefficients() {
        // Test empty
        val empty = Polynomial.fromCoefficients()
        assertEquals(Polynomial.of(), empty)

        // Test single coefficient (constant)
        val constant = Polynomial.fromCoefficients(5f)
        assertEquals(Polynomial.of("5"), constant)

        // Test multiple coefficients
        val poly = Polynomial.fromCoefficients(1f, 2f, 3f, 4f)
        assertEquals(Polynomial.of("4x^3 + 3x^2 + 2x + 1"), poly)
    }

    @Test
    fun ofString() {
        // Test "x^2" -> x^2
        val poly1 = Polynomial.of("x^2")
        assertEquals(1, poly1.terms.size)
        assertEquals(1f, poly1.terms[0].coefficient, 0.001f)
        assertEquals(2, poly1.terms[0].exponent)

        // Test "x" -> x
        val poly2 = Polynomial.of("x")
        assertEquals(1, poly2.terms.size)
        assertEquals(1f, poly2.terms[0].coefficient, 0.001f)
        assertEquals(1, poly2.terms[0].exponent)

        // Test "x + 4x^2 + 5" -> 5 + x + 4x^2
        val poly3 = Polynomial.of("x + 4x^2 + 5")
        assertEquals(3, poly3.terms.size)

        // Test "-2x" -> -2x
        val poly4 = Polynomial.of("-2x")
        assertEquals(1, poly4.terms.size)
        assertEquals(-2f, poly4.terms[0].coefficient, 0.001f)
        assertEquals(1, poly4.terms[0].exponent)

        // Test "x - 2x^3" -> x - 2x^3
        val poly5 = Polynomial.of("x - 2x^3")
        assertEquals(2, poly5.terms.size)

        // Test constant "5" -> 5
        val poly6 = Polynomial.of("5")
        assertEquals(1, poly6.terms.size)
        assertEquals(5f, poly6.terms[0].coefficient, 0.001f)
        assertEquals(0, poly6.terms[0].exponent)

        // Test with spaces "2x^3 + 3x^2 - 5x + 7"
        val poly7 = Polynomial.of("2x^3 + 3x^2 - 5x + 7")
        assertEquals(4, poly7.terms.size)
    }

    @Test
    fun toStringTest() {
        val poly = Polynomial.of("2x^3 - 4x^2 + x - 5.1234")
        assertEquals("2x^3 - 4x^2 + x - 5.1234", poly.toString())
    }
}