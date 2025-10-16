package com.kylecorry.sol.math.interpolation

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class NewtonInterpolatorTest {

    @Test
    fun interpolate() {
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolateWithPoints(0f, xs, ys))
        assertEquals(1f, interpolator.interpolateWithPoints(1f, xs, ys))
        assertEquals(8f, interpolator.interpolateWithPoints(2f, xs, ys))
        assertEquals(27f, interpolator.interpolateWithPoints(3f, xs, ys))
        assertEquals(64f, interpolator.interpolateWithPoints(4f, xs, ys))
        assertEquals(125f, interpolator.interpolateWithPoints(5f, xs, ys))
        assertEquals(0.125f, interpolator.interpolateWithPoints(0.5f, xs, ys))
        assertEquals(3.375f, interpolator.interpolateWithPoints(1.5f, xs, ys))
        assertEquals(15.625f, interpolator.interpolateWithPoints(2.5f, xs, ys))
        assertEquals(-1f, interpolator.interpolateWithPoints(-1f, xs, ys))
        assertEquals(-0.125f, interpolator.interpolateWithPoints(-0.5f, xs, ys))
    }

    @Test
    fun firstOrderInterpolation(){
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolateWithPoints(0f, xs, ys, 1))
        assertEquals(1f, interpolator.interpolateWithPoints(1f, xs, ys, 1))
        assertEquals(2f, interpolator.interpolateWithPoints(2f, xs, ys, 1))
        assertEquals(3f, interpolator.interpolateWithPoints(3f, xs, ys, 1))
        assertEquals(4f, interpolator.interpolateWithPoints(4f, xs, ys, 1))
        assertEquals(5f, interpolator.interpolateWithPoints(5f, xs, ys, 1))
        assertEquals(0.5f, interpolator.interpolateWithPoints(0.5f, xs, ys, 1))
        assertEquals(1.5f, interpolator.interpolateWithPoints(1.5f, xs, ys, 1))
    }

    @Test
    fun secondOrderInterpolation(){
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolateWithPoints(0f, xs, ys, 2))
        assertEquals(1f, interpolator.interpolateWithPoints(1f, xs, ys, 2))
        assertEquals(8f, interpolator.interpolateWithPoints(2f, xs, ys, 2))
        assertEquals(21f, interpolator.interpolateWithPoints(3f, xs, ys, 2))
        assertEquals(40f, interpolator.interpolateWithPoints(4f, xs, ys, 2))
        assertEquals(65f, interpolator.interpolateWithPoints(5f, xs, ys, 2))
        assertEquals(-0.25f, interpolator.interpolateWithPoints(0.5f, xs, ys, 2))
        assertEquals(3.75f, interpolator.interpolateWithPoints(1.5f, xs, ys, 2))
    }

    @Test
    fun interpolateWithAutoSelection() {
        val interpolator = NewtonInterpolator()
        // Test with a larger dataset where auto-selection matters
        val xs = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)
        val ys = xs.map { it * it } // y = x^2

        // Test cubic interpolation (order 3, uses 4 points)
        assertEquals(0f, interpolator.interpolate(0f, xs, ys, 3), 0.01f)
        assertEquals(1f, interpolator.interpolate(1f, xs, ys, 3), 0.01f)
        assertEquals(4f, interpolator.interpolate(2f, xs, ys, 3), 0.01f)
        assertEquals(25f, interpolator.interpolate(5f, xs, ys, 3), 0.01f)
        assertEquals(100f, interpolator.interpolate(10f, xs, ys, 3), 0.01f)
        
        // Test intermediate values
        assertEquals(2.25f, interpolator.interpolate(1.5f, xs, ys, 3), 0.01f)
        assertEquals(6.25f, interpolator.interpolate(2.5f, xs, ys, 3), 0.01f)
    }

    @Test
    fun interpolateWithAutoSelectionLinear() {
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)
        val ys = xs.map { it * 2f } // y = 2x

        // Test linear interpolation (order 1, uses 2 points)
        assertEquals(0f, interpolator.interpolate(0f, xs, ys, 1), 0.01f)
        assertEquals(2f, interpolator.interpolate(1f, xs, ys, 1), 0.01f)
        assertEquals(10f, interpolator.interpolate(5f, xs, ys, 1), 0.01f)
        assertEquals(20f, interpolator.interpolate(10f, xs, ys, 1), 0.01f)
        
        // Test intermediate values
        assertEquals(3f, interpolator.interpolate(1.5f, xs, ys, 1), 0.01f)
        assertEquals(5f, interpolator.interpolate(2.5f, xs, ys, 1), 0.01f)
    }

    @Test
    fun interpolateWithAutoSelectionEdgeCases() {
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 4f, 9f, 16f)

        // Test at the edges of the dataset
        assertEquals(0f, interpolator.interpolate(0f, xs, ys, 2), 0.01f)
        assertEquals(16f, interpolator.interpolate(4f, xs, ys, 2), 0.01f)

        // Test extrapolation beyond the dataset
        // Note: Behavior may vary, but should not crash
        val result = interpolator.interpolate(5f, xs, ys, 2)
        assertTrue(result.isFinite())
    }
}