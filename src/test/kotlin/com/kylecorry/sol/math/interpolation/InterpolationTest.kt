package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InterpolationTest {

    @Test
    fun resample() {
        val points = listOf(
            Vector2(0f, 0f),
            Vector2(1f, 1f),
            Vector2(2f, 8f),
            Vector2(3f, 27f),
            Vector2(4f, 64f)
        )
        val interpolator = LinearInterpolator(points)
        val resampled = Interpolation.resample(interpolator, 0f, 2f, 0.5f)
        val expected = listOf(
            Vector2(0f, 0f),
            Vector2(0.5f, 0.5f),
            Vector2(1f, 1f),
            Vector2(1.5f, 4.5f),
            Vector2(2f, 8f)
        )
        assertEquals(expected.size, resampled.size)
        for (i in expected.indices) {
            assertEquals(expected[i].x, resampled[i].x, 0.0001f)
            assertEquals(expected[i].y, resampled[i].y, 0.0001f)
        }
    }

}