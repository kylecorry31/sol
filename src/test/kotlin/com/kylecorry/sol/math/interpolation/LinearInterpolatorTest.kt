package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LinearInterpolatorTest {

    @Test
    fun interpolate() {
        val interpolator = LinearInterpolator(
            listOf(
                Vector2(0f, 0f),
                Vector2(1f, 1f),
                Vector2(2f, 8f),
                Vector2(3f, 27f),
                Vector2(4f, 64f)
            )
        )
        assertEquals(-1f, interpolator.interpolate(-1f))
        assertEquals(0f, interpolator.interpolate(0f))
        assertEquals(1f, interpolator.interpolate(1f))
        assertEquals(8f, interpolator.interpolate(2f))
        assertEquals(27f, interpolator.interpolate(3f))
        assertEquals(64f, interpolator.interpolate(4f))
        assertEquals(101f, interpolator.interpolate(5f))
        assertEquals(0.5f, interpolator.interpolate(0.5f))
        assertEquals(4.5f, interpolator.interpolate(1.5f))
    }
}