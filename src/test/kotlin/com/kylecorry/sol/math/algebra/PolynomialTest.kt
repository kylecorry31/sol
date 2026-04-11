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

    @Test
    fun addition() {
        // Test: (2x + 3) + (x + 1) = 3x + 4
        val poly1 = Polynomial.of("2x + 3")
        val poly2 = Polynomial.of("x + 1")
        val result = poly1 + poly2
        assertEquals(Polynomial.of("3x + 4"), result)

        // Test: (x^2 + 2x + 1) + (x^2 + x - 1) = 2x^2 + 3x
        val quadratic1 = Polynomial.fromCoefficients(1f, 2f, 1f)
        val quadratic2 = Polynomial.of("x^2 + x - 1")
        val result2 = quadratic1 + quadratic2
        assertEquals(Polynomial.of("2x^2 + 3x"), result2)

        // Test: (5x^3 + 2x) + (-5x^3 + 3x) = 5x
        val cubic1 = Polynomial.of("5x^3 + 2x")
        val cubic2 = Polynomial.of("-5x^3 + 3x")
        val result3 = cubic1 + cubic2
        assertEquals(Polynomial.of("5x"), result3)

        // Test: (3) + (7) = 10 (constant addition)
        val const1 = Polynomial.of("3")
        val const2 = Polynomial.of("7")
        val result4 = const1 + const2
        assertEquals(Polynomial.of("10"), result4)

        // Test: (x^2) + (0) = x^2 (adding zero)
        val quadratic = Polynomial.of("x^2")
        val zero = Polynomial.of()
        val result5 = quadratic + zero
        assertEquals(quadratic, result5)

        // Test by evaluation: (x^2 + x + 1) + (2x - 1) at x=2 should equal 10
        val p1 = Polynomial.of("x^2 + x + 1")
        val p2 = Polynomial.of("2x - 1")
        val sum = p1 + p2
        assertEquals(10f, sum.evaluate(2f), 0.001f)
    }

    @Test
    fun subtraction() {
        // Test: (3x + 5) - (x + 2) = 2x + 3
        val poly1 = Polynomial.of("3x + 5")
        val poly2 = Polynomial.of("x + 2")
        val result = poly1 - poly2
        assertEquals(Polynomial.of("2x + 3"), result)

        // Test: (x^2 + 3x + 2) - (x^2 + x - 1) = 2x + 3
        val quadratic1 = Polynomial.of("x^2 + 3x + 2")
        val quadratic2 = Polynomial.of("x^2 + x - 1")
        val result2 = quadratic1 - quadratic2
        assertEquals(Polynomial.of("2x + 3"), result2)

        // Test: (5x^3 + 2x) - (2x^3 + 3x) = 3x^3 - x
        val cubic1 = Polynomial.of("5x^3 + 2x")
        val cubic2 = Polynomial.of("2x^3 + 3x")
        val result3 = cubic1 - cubic2
        assertEquals(Polynomial.of("3x^3 - x"), result3)

        // Test: (10) - (3) = 7 (constant subtraction)
        val const1 = Polynomial.of("10")
        val const2 = Polynomial.of("3")
        val result4 = const1 - const2
        assertEquals(Polynomial.of("7"), result4)

        // Test: (x^2) - (x^2) = 0
        val quadratic = Polynomial.of("x^2")
        val result5 = quadratic - quadratic
        assertEquals(Polynomial.of("0"), result5)

        // Test by evaluation: (3x^2 + 2x + 5) - (x^2 + x + 1) at x=3 should equal 25
        val p1 = Polynomial.of("3x^2 + 2x + 5")
        val p2 = Polynomial.of("x^2 + x + 1")
        val diff = p1 - p2
        assertEquals(25f, diff.evaluate(3f), 0.001f)
    }

    @Test
    fun multiplication() {
        // Test: (2x + 1) * (x + 3) = 2x^2 + 7x + 3
        val poly1 = Polynomial.of("2x + 1")
        val poly2 = Polynomial.of("x + 3")
        val result = poly1 * poly2
        assertEquals(Polynomial.of("2x^2 + 7x + 3"), result)

        // Test: (x + 1) * (x - 1) = x^2 - 1
        val binomial1 = Polynomial.of("x + 1")
        val binomial2 = Polynomial.of("x - 1")
        val result2 = binomial1 * binomial2
        assertEquals(Polynomial.of("x^2 - 1"), result2)

        // Test: (x + 2)^2 = x^2 + 4x + 4
        val binomial = Polynomial.of("x + 2")
        val result3 = binomial * binomial
        assertEquals(Polynomial.of("x^2 + 4x + 4"), result3)

        // Test: (3x^2 + 2x + 1) * (2x + 1) = 6x^3 + 7x^2 + 4x + 1
        val quadratic = Polynomial.of("3x^2 + 2x + 1")
        val linear = Polynomial.of("2x + 1")
        val result4 = quadratic * linear
        assertEquals(Polynomial.of("6x^3 + 7x^2 + 4x + 1"), result4)

        // Test: (5) * (3) = 15 (constant multiplication)
        val const1 = Polynomial.of("5")
        val const2 = Polynomial.of("3")
        val result5 = const1 * const2
        assertEquals(Polynomial.of("15"), result5)

        // Test: (x) * (0) = 0
        val x = Polynomial.of("x")
        val zero = Polynomial.of()
        val result6 = x * zero
        assertEquals(Polynomial.of("0"), result6)

        // Test by evaluation: (x + 2) * (x - 1) at x=4 should equal 18
        val p1 = Polynomial.of("x + 2")
        val p2 = Polynomial.of("x - 1")
        val product = p1 * p2
        assertEquals(18f, product.evaluate(4f), 0.001f)

        // Test: (x^2 + 1) * (x^2 - 1) = x^4 - 1
        val sq1 = Polynomial.of("x^2 + 1")
        val sq2 = Polynomial.of("x^2 - 1")
        val result7 = sq1 * sq2
        assertEquals(Polynomial.of("x^4 - 1"), result7)
    }
}