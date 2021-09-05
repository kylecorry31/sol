package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class SolMathTest {

    @ParameterizedTest
    @MethodSource("provideWrapDouble")
    fun wrapDouble(value: Double, min: Double, max: Double, expected: Double) {
        val actual = SolMath.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideWrapFloat")
    fun wrapFloat(value: Float, min: Float, max: Float, expected: Float) {
        val actual = SolMath.wrap(value, min, max)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("providePower")
    fun power(value: Double, exponent: Int, expected: Double) {
        val actual = SolMath.power(value, exponent)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("providePowerInt")
    fun power(value: Int, exponent: Int, expected: Int) {
        val actual = SolMath.power(value, exponent)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("providePolynomial")
    fun polynomial(x: Double, coefs: DoubleArray, expected: Double) {
        val actual = SolMath.polynomial(x, *coefs)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideCube")
    fun cube(x: Double, expected: Double) {
        val actual = SolMath.cube(x)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideSquare")
    fun square(x: Double, expected: Double) {
        val actual = SolMath.square(x)
        assertEquals(expected, actual, 0.00001)
    }

    @Test
    fun interpolate() {
        assertEquals(
            0.876125,
            SolMath.interpolate(0.18125, 0.884226, 0.877366, 0.870531),
            0.0000005
        )
    }

    @Test
    fun normalizeAngle() {
        assertEquals(0.0, SolMath.normalizeAngle(0.0), 0.0)
        assertEquals(180.0, SolMath.normalizeAngle(180.0), 0.0)
        assertEquals(0.0, SolMath.normalizeAngle(0.0), 0.0)
        assertEquals(1.0, SolMath.normalizeAngle(361.0), 0.0)
        assertEquals(359.0, SolMath.normalizeAngle(-1.0), 0.0)
        assertEquals(180.0, SolMath.normalizeAngle(-180.0), 0.0)
        assertEquals(0.0, SolMath.normalizeAngle(720.0), 0.0)
    }

    @ParameterizedTest
    @MethodSource("provideDeltaAngle")
    fun square(angle1: Float, angle2: Float, expected: Float) {
        val actual = SolMath.deltaAngle(angle1, angle2)
        assertEquals(expected, actual, 0.00001f)
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
                Arguments.of(6.0, 5.0, 4.0, 6.0),
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
                Arguments.of(6.0f, 5.0f, 4.0f, 6.0f),
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
        fun provideDeltaAngle(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.0f, 0.0f, 0.0f),
                Arguments.of(15.0f, 15.0f, 0.0f),
                Arguments.of(15.0f, 25.0f, 10.0f),
                Arguments.of(25.0f, 15.0f, -10.0f),
                Arguments.of(0.0f, 360.0f, 0.0f),
                Arguments.of(0.0f, 720.0f, 0.0f),
                Arguments.of(10.0f, 370.0f, 0.0f),
                Arguments.of(-10.0f, 370.0f, 20.0f),
                Arguments.of(370.0f, -10.0f, -20.0f),
                Arguments.of(10.0f, 180.0f, 170.0f),
                Arguments.of(-10.0f, 180.0f, -170.0f),
                Arguments.of(0.0f, 180.0f, 180.0f),
            )
        }
    }
}