package com.kylecorry.sol.math.algebra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class AlgebraTest {

    @Test
    fun solveLinear() {
        assertEquals(-2.0f, Algebra.solve(LinearEquation(0.25f, 0.5f))!!, 0.0001f)
        assertEquals(0.0f, Algebra.solve(LinearEquation(0.0f, 0.0f))!!, 0.0001f)
        assertNull(Algebra.solve(LinearEquation(0.0f, 2.0f)))
    }

    @Test
    fun solveQuadratic() {
        assertEquals(0.0f to 0.0f, Algebra.solve(QuadraticEquation(1f, 0f, 0f))!!, 0.00001f)
        assertEquals(
            0.39039f to -0.64039f,
            Algebra.solve(QuadraticEquation(8f, 2f, -2f))!!,
            0.00001f
        )
        assertNull(Algebra.solve(QuadraticEquation(8f, -2f, 5f)))
    }

    @Test
    fun inverse() {
        val inverted = Algebra.inverse(LinearEquation(5f, 2f))
        assertEquals(0.2f to -0.4f, inverted.m to inverted.b, 0.0001f)
    }

    @ParameterizedTest
    @MethodSource("providePolynomial")
    fun polynomial(x: Double, coefs: DoubleArray, expected: Double) {
        val actual = Algebra.polynomial(x, *coefs)
        assertEquals(expected, actual, 0.00001)
    }

    fun assertEquals(
        expected: Pair<Float, Float>,
        actual: Pair<Float, Float>,
        tolerance: Float = 0.0f
    ) {
        assertEquals(expected.first, actual.first, tolerance)
        assertEquals(expected.second, actual.second, tolerance)
    }

    companion object {
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
    }
}