package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LocalNewtonInterpolatorTest {

    @Test
    fun firstOrderInterpolation() {
        val interpolator = LocalNewtonInterpolator(
            listOf(
                Vector2(0f, 0f),
                Vector2(1f, 1f),
                Vector2(2f, 8f),
                Vector2(3f, 27f),
                Vector2(4f, 64f)
            ),
            1
        )
        assertEquals(0f, interpolator.interpolate(0f))
        assertEquals(1f, interpolator.interpolate(1f))
        assertEquals(8f, interpolator.interpolate(2f))
        assertEquals(27f, interpolator.interpolate(3f))
        assertEquals(64f, interpolator.interpolate(4f))
        assertEquals(101f, interpolator.interpolate(5f))
        assertEquals(0.5f, interpolator.interpolate(0.5f))
        assertEquals(4.5f, interpolator.interpolate(1.5f))
    }

    @Test
    fun secondOrderInterpolation() {
        val interpolator = LocalNewtonInterpolator(
            listOf(
                Vector2(0f, 0f),
                Vector2(1f, 1f),
                Vector2(2f, 8f),
                Vector2(3f, 27f),
                Vector2(4f, 64f)
            ),
            2
        )
        assertEquals(0f, interpolator.interpolate(0f))
        assertEquals(1f, interpolator.interpolate(1f))
        assertEquals(8f, interpolator.interpolate(2f))
        assertEquals(27f, interpolator.interpolate(3f))
        assertEquals(64f, interpolator.interpolate(4f))
        assertEquals(119f, interpolator.interpolate(5f))
        assertEquals(-0.25f, interpolator.interpolate(0.5f))
        assertEquals(3.75f, interpolator.interpolate(1.5f))
    }

    @Test
    fun thirdOrderInterpolation() {
        val interpolator = LocalNewtonInterpolator(
            listOf(
                Vector2(0f, 0f),
                Vector2(1f, 1f),
                Vector2(2f, 8f),
                Vector2(3f, 27f),
                Vector2(4f, 64f)
            ),
            3
        )
        assertEquals(0f, interpolator.interpolate(0f))
        assertEquals(1f, interpolator.interpolate(1f))
        assertEquals(8f, interpolator.interpolate(2f))
        assertEquals(27f, interpolator.interpolate(3f))
        assertEquals(64f, interpolator.interpolate(4f))
        assertEquals(125f, interpolator.interpolate(5f))
        assertEquals(0.125f, interpolator.interpolate(0.5f))
        assertEquals(3.375f, interpolator.interpolate(1.5f))
    }
}