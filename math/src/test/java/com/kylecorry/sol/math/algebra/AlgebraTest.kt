package com.kylecorry.sol.math.algebra

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

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

    fun assertEquals(
        expected: Pair<Float, Float>,
        actual: Pair<Float, Float>,
        tolerance: Float = 0.0f
    ) {
        assertEquals(expected.first, actual.first, tolerance)
        assertEquals(expected.second, actual.second, tolerance)
    }
}