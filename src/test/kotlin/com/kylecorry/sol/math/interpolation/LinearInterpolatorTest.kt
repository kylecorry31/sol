package com.kylecorry.sol.math.interpolation

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class LinearInterpolatorTest {

    @Test
    fun interpolate(){
        val interpolator = LinearInterpolator()
        val xs = listOf(0f, 1f, 2f, 3f, 4f)
        val ys = listOf(0f, 1f, 8f, 27f, 64f)
        assertEquals(0f, interpolator.interpolate(-1f, xs, ys))
        assertEquals(0f, interpolator.interpolate(0f, xs, ys))
        assertEquals(1f, interpolator.interpolate(1f, xs, ys))
        assertEquals(8f, interpolator.interpolate(2f, xs, ys))
        assertEquals(27f, interpolator.interpolate(3f, xs, ys))
        assertEquals(64f, interpolator.interpolate(4f, xs, ys))
        assertEquals(64f, interpolator.interpolate(5f, xs, ys))
        assertEquals(0.5f, interpolator.interpolate(0.5f, xs, ys))
        assertEquals(4.5f, interpolator.interpolate(1.5f, xs, ys))
    }
}