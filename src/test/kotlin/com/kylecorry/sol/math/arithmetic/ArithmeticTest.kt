package com.kylecorry.sol.math.arithmetic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

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
    @MethodSource("provideWrapDouble")
    fun wrapDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = Arithmetic.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideWrapFloat")
    fun wrapFloat(value: Float, min: Float, max: Float, expected: Float) {
        val actual = Arithmetic.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("providePower")
    fun power(value: Double, exponent: Int, expected: Double) {
        val actual = Arithmetic.power(value, exponent)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("providePowerInt")
    fun power(value: Int, exponent: Int, expected: Int) {
        val actual = Arithmetic.power(value, exponent)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("providePolynomial")
    fun polynomial(x: Double, coefs: DoubleArray, expected: Double) {
        val actual = Arithmetic.polynomial(x, *coefs)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideCube")
    fun cube(x: Double, expected: Double) {
        val actual = Arithmetic.cube(x)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideSquare")
    fun square(x: Double, expected: Double) {
        val actual = Arithmetic.square(x)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideClamp")
    fun clampDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = Arithmetic.clamp(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideClamp")
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

    companion object {
        @JvmStatic
        fun provideWrapDouble(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.2, 0.0, 1.0, 0.2),
                Arguments.of(1.0, 0.0, 1.0, 1.0),
                Arguments.of(0.0, 0.0, 1.0, 0.0),
                Arguments.of(1.5, 0.0, 1.0, 0.5),
                Arguments.of(-0.75, 0.0, 1.0, 0.25),
                Arguments.of(0.0, 1.0, 4.0, 3.0),
                Arguments.of(5.0, 1.0, 4.0, 2.0),
                Arguments.of(6.0, 5.0, 4.0, 5.0),
                Arguments.of(0.0, 0.0, 0.0, 0.0),
                Arguments.of(1800.0, 0.0, 360.0, 0.0),
                Arguments.of(-1800.0, 0.0, 360.0, 360.0),
                Arguments.of(1799.0, 0.0, 360.0, 359.0),
                Arguments.of(-1799.0, 0.0, 360.0, 1.0),
            )
        }

        @JvmStatic
        fun provideWrapFloat(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.2f, 0.0f, 1.0f, 0.2f),
                Arguments.of(1.0f, 0.0f, 1.0f, 1.0f),
                Arguments.of(0.0f, 0.0f, 1.0f, 0.0f),
                Arguments.of(1.5f, 0.0f, 1.0f, 0.5f),
                Arguments.of(-0.75f, 0.0f, 1.0f, 0.25f),
                Arguments.of(0.0f, 1.0f, 4.0f, 3.0f),
                Arguments.of(5.0f, 1.0f, 4.0f, 2.0f),
                Arguments.of(6.0f, 5.0f, 4.0f, 5.0f),
                Arguments.of(0.0f, 0.0f, 0.0f, 0.0f),
                Arguments.of(1800.0f, 0.0f, 360.0f, 0.0f),
                Arguments.of(-1800.0f, 0.0f, 360.0f, 360.0f),
                Arguments.of(1799.0f, 0.0f, 360.0f, 359.0f),
                Arguments.of(-1799.0f, 0.0f, 360.0f, 1.0f),
                Arguments.of(-250f, -180f, 180f, 110f),
            )
        }

        @JvmStatic
        fun providePower(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, 2, 1.0),
                Arguments.of(1.0, 0, 1.0),
                Arguments.of(1.0, -1, 1.0),
                Arguments.of(3.0, -1, 1 / 3.0),
                Arguments.of(3.0, -2, 1 / 9.0),
                Arguments.of(3.0, 0, 1.0),
                Arguments.of(3.0, 1, 3.0),
                Arguments.of(3.0, 2, 9.0),
                Arguments.of(0.0, 2, 0.0),
                Arguments.of(-2.0, 2, 4.0),
                Arguments.of(-2.0, 3, -8.0),
                Arguments.of(0.5, 2, 0.25),
                Arguments.of(0.5, -2, 4.0),
            )
        }

        @JvmStatic
        fun providePowerInt(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1, 2, 1),
                Arguments.of(1, 0, 1),
                Arguments.of(1, -1, 1),
                Arguments.of(3, -1, 0),
                Arguments.of(3, -2, 0),
                Arguments.of(3, 0, 1),
                Arguments.of(3, 1, 3),
                Arguments.of(3, 2, 9),
                Arguments.of(-2, 2, 4),
                Arguments.of(-2, 3, -8),
                Arguments.of(0, 3, 0)
            )
        }

        @JvmStatic
        fun providePolynomial(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, doubleArrayOf(1.0, 2.0, 3.0), 6.0),
                Arguments.of(2.0, doubleArrayOf(1.0, 2.0, 3.0), 17.0),
                Arguments.of(3.0, doubleArrayOf(0.0, 1.0, 3.0, 1.0), 57.0),
                Arguments.of(3.0, doubleArrayOf(0.0, 1.0, -3.0, 1.0), 3.0),
                Arguments.of(3.0, doubleArrayOf(), 0.0),
                Arguments.of(3.0, doubleArrayOf(1.0), 1.0),
            )
        }

        @JvmStatic
        fun provideCube(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, 1.0),
                Arguments.of(2.0, 8.0),
                Arguments.of(3.0, 27.0),
                Arguments.of(4.0, 64.0),
                Arguments.of(-4.0, -64.0),
                Arguments.of(0.0, 0.0),
                Arguments.of(0.5, 0.125),
            )
        }

        @JvmStatic
        fun provideSquare(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.0, 1.0),
                Arguments.of(2.0, 4.0),
                Arguments.of(3.0, 9.0),
                Arguments.of(4.0, 16.0),
                Arguments.of(-4.0, 16.0),
                Arguments.of(0.0, 0.0),
                Arguments.of(0.5, 0.25),
            )
        }

        @JvmStatic
        fun provideClamp(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, 0.1),
                Arguments.of(0.0, 0.0, 1.0, 0.0),
                Arguments.of(1.0, 0.0, 1.0, 1.0),
                Arguments.of(1.2, 0.0, 1.0, 1.0),
                Arguments.of(-0.1, 0.0, 1.0, 0.0),
                Arguments.of(4.0, 2.0, 5.0, 4.0),
                Arguments.of(1.0, 2.0, 5.0, 2.0),
                Arguments.of(6.0, 2.0, 5.0, 5.0),
            )
        }
    }
}