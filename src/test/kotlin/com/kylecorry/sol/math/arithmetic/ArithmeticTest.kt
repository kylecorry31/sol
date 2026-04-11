package com.kylecorry.sol.math.arithmetic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ArithmeticTest {
    @ParameterizedTest
    @CsvSource(
        "0, 1",
        "1, 1",
        "2, 2",
        "3, 6",
        "4, 24",
        "5, 120",
        "20, 2432902008176640000",
        "-1, -1",
        "-5, -120",
        "-20, -2432902008176640000"
    )
    fun factorial(n: Int, expected: Long) {
        assertEquals(expected, Arithmetic.factorial(n))
    }

    @ParameterizedTest
    @CsvSource(
        "0.2, 0.0, 1.0, 0.2",
        "1.0, 0.0, 1.0, 1.0",
        "0.0, 0.0, 1.0, 0.0",
        "1.5, 0.0, 1.0, 0.5",
        "-0.75, 0.0, 1.0, 0.25",
        "0.0, 1.0, 4.0, 3.0",
        "5.0, 1.0, 4.0, 2.0",
        "6.0, 5.0, 4.0, 5.0",
        "0.0, 0.0, 0.0, 0.0",
        "1800.0, 0.0, 360.0, 0.0",
        "-1800.0, 0.0, 360.0, 360.0",
        "1799.0, 0.0, 360.0, 359.0",
        "-1799.0, 0.0, 360.0, 1.0",
    )
    fun wrapDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = Arithmetic.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "0.2, 0.0, 1.0, 0.2",
        "1.0, 0.0, 1.0, 1.0",
        "0.0, 0.0, 1.0, 0.0",
        "1.5, 0.0, 1.0, 0.5",
        "-0.75, 0.0, 1.0, 0.25",
        "0.0, 1.0, 4.0, 3.0",
        "5.0, 1.0, 4.0, 2.0",
        "6.0, 5.0, 4.0, 5.0",
        "0.0, 0.0, 0.0, 0.0",
        "1800.0, 0.0, 360.0, 0.0",
        "-1800.0, 0.0, 360.0, 360.0",
        "1799.0, 0.0, 360.0, 359.0",
        "-1799.0, 0.0, 360.0, 1.0",
        "-250.0, -180.0, 180.0, 110.0",
    )
    fun wrapFloat(value: Float, min: Float, max: Float, expected: Float) {
        val actual = Arithmetic.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 2, 1.0",
        "1.0, 0, 1.0",
        "1.0, -1, 1.0",
        "3.0, -1, 0.3333333333333333",
        "3.0, -2, 0.1111111111111111",
        "3.0, 0, 1.0",
        "3.0, 1, 3.0",
        "3.0, 2, 9.0",
        "0.0, 2, 0.0",
        "-2.0, 2, 4.0",
        "-2.0, 3, -8.0",
        "0.5, 2, 0.25",
        "0.5, -2, 4.0",
    )
    fun power(value: Double, exponent: Int, expected: Double) {
        val actual = Arithmetic.power(value, exponent)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 2, 1",
        "1, 0, 1",
        "1, -1, 1",
        "3, -1, 0",
        "3, -2, 0",
        "3, 0, 1",
        "3, 1, 3",
        "3, 2, 9",
        "-2, 2, 4",
        "-2, 3, -8",
        "0, 3, 0",
    )
    fun powerInt(value: Int, exponent: Int, expected: Int) {
        val actual = Arithmetic.power(value, exponent)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0",
        "2.0, 8.0",
        "3.0, 27.0",
        "4.0, 64.0",
        "-4.0, -64.0",
        "0.0, 0.0",
        "0.5, 0.125",
    )
    fun cube(x: Double, expected: Double) {
        val actual = Arithmetic.cube(x)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0",
        "2.0, 4.0",
        "3.0, 9.0",
        "4.0, 16.0",
        "-4.0, 16.0",
        "0.0, 0.0",
        "0.5, 0.25",
    )
    fun square(x: Double, expected: Double) {
        val actual = Arithmetic.square(x)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "0.1, 0.0, 1.0, 0.1",
        "0.0, 0.0, 1.0, 0.0",
        "1.0, 0.0, 1.0, 1.0",
        "1.2, 0.0, 1.0, 1.0",
        "-0.1, 0.0, 1.0, 0.0",
        "4.0, 2.0, 5.0, 4.0",
        "1.0, 2.0, 5.0, 2.0",
        "6.0, 2.0, 5.0, 5.0",
    )
    fun clampDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = Arithmetic.clamp(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @CsvSource(
        "0.1, 0.0, 1.0, 0.1",
        "0.0, 0.0, 1.0, 0.0",
        "1.0, 0.0, 1.0, 1.0",
        "1.2, 0.0, 1.0, 1.0",
        "-0.1, 0.0, 1.0, 0.0",
        "4.0, 2.0, 5.0, 4.0",
        "1.0, 2.0, 5.0, 2.0",
        "6.0, 2.0, 5.0, 5.0",
    )
    fun clampFloat(value: Double, min: Double, max: Double, expected: Double) {
        val actual = Arithmetic.clamp(value.toFloat(), min.toFloat(), max.toFloat())
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1",
        "100, 200, 100",
        "200, 100, 100",
        "0, 0, 0",
        "0, 1, 1",
        "1, 0, 1",
        "-1, 1, 1",
        "400, 600, 200",
    )
    fun greatestCommonDivisor(a: Int, b: Int, expected: Int) {
        val actual = Arithmetic.greatestCommonDivisor(a, b)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1",
        "100, 200, 200",
        "200, 100, 200",
        "0, 0, 0",
        "0, 1, 0",
        "1, 0, 0",
        "-1, 1, 1",
        "400, 600, 1200",
        "3800, 7600, 7600"
    )
    fun leastCommonMultiple(a: Int, b: Int, expected: Int) {
        val actual = Arithmetic.leastCommonMultiple(a, b)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0, 1.0",
        "1.5, 1.0, 0.5",
        "1.4, 1.0, 0.2",
        "0.01, 3.8, 0.01",
        "0.0, 1.0, 1.0",
        "1.0, 0.0, 1.0",
        "0.0, 0.0, 0.0",
    )
    fun greatestCommonDivisorFloat(a: Float, b: Float, expected: Float) {
        val actual = Arithmetic.greatestCommonDivisor(a, b)
        assertEquals(expected, actual, 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 1.0, 1.0",
        "1.5, 1.0, 3.0",
        "1.4, 1.0, 7.0",
        "0.01, 3.8, 3.8",
        "0.0, 1.0, 0.0",
        "1.0, 0.0, 0.0",
        "0.0, 0.0, 0.0",
    )
    fun leastCommonMultipleFloat(a: Float, b: Float, expected: Float) {
        val actual = Arithmetic.leastCommonMultiple(a, b)
        assertEquals(expected, actual, 0.001f)
    }
}
