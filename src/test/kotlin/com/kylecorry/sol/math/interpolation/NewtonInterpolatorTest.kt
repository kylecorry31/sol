package com.kylecorry.sol.math.interpolation

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class NewtonInterpolatorTest {

    @Test
    fun interpolate() {
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolate(0f, xs, ys))
        assertEquals(1f, interpolator.interpolate(1f, xs, ys))
        assertEquals(8f, interpolator.interpolate(2f, xs, ys))
        assertEquals(27f, interpolator.interpolate(3f, xs, ys))
        assertEquals(64f, interpolator.interpolate(4f, xs, ys))
        assertEquals(125f, interpolator.interpolate(5f, xs, ys))
        assertEquals(0.125f, interpolator.interpolate(0.5f, xs, ys))
        assertEquals(3.375f, interpolator.interpolate(1.5f, xs, ys))
        assertEquals(15.625f, interpolator.interpolate(2.5f, xs, ys))
        assertEquals(-1f, interpolator.interpolate(-1f, xs, ys))
        assertEquals(-0.125f, interpolator.interpolate(-0.5f, xs, ys))
    }

    @Test
    fun firstOrderInterpolation(){
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolate(0f, xs, ys, 1))
        assertEquals(1f, interpolator.interpolate(1f, xs, ys, 1))
        assertEquals(2f, interpolator.interpolate(2f, xs, ys, 1))
        assertEquals(3f, interpolator.interpolate(3f, xs, ys, 1))
        assertEquals(4f, interpolator.interpolate(4f, xs, ys, 1))
        assertEquals(5f, interpolator.interpolate(5f, xs, ys, 1))
        assertEquals(0.5f, interpolator.interpolate(0.5f, xs, ys, 1))
        assertEquals(1.5f, interpolator.interpolate(1.5f, xs, ys, 1))
    }

    @Test
    fun secondOrderInterpolation(){
        val interpolator = NewtonInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolate(0f, xs, ys, 2))
        assertEquals(1f, interpolator.interpolate(1f, xs, ys, 2))
        assertEquals(8f, interpolator.interpolate(2f, xs, ys, 2))
        assertEquals(21f, interpolator.interpolate(3f, xs, ys, 2))
        assertEquals(40f, interpolator.interpolate(4f, xs, ys, 2))
        assertEquals(65f, interpolator.interpolate(5f, xs, ys, 2))
        assertEquals(-0.25f, interpolator.interpolate(0.5f, xs, ys, 2))
        assertEquals(3.75f, interpolator.interpolate(1.5f, xs, ys, 2))
    }
}