package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

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

    @Test
    fun linearVector() {
        val point1 = Vector2(0f, 0f)
        val point2 = Vector2(2f, 4f)

        assertEquals(0f, Interpolation.linear(0f, point1, point2), 0.0001f)
        assertEquals(2f, Interpolation.linear(1f, point1, point2), 0.0001f)
        assertEquals(4f, Interpolation.linear(2f, point1, point2), 0.0001f)
        assertEquals(1f, Interpolation.linear(0.5f, point1, point2), 0.0001f)
        val point3 = Vector2(-2f, -4f)
        val point4 = Vector2(2f, 4f)
        assertEquals(0f, Interpolation.linear(0f, point3, point4), 0.0001f)
        assertEquals(-2f, Interpolation.linear(-1f, point3, point4), 0.0001f)
    }

    @Test
    fun linear() {
        assertEquals(0f, Interpolation.linear(0f, 0f, 0f, 2f, 4f), 0.0001f)
        assertEquals(2f, Interpolation.linear(1f, 0f, 0f, 2f, 4f), 0.0001f)
        assertEquals(4f, Interpolation.linear(2f, 0f, 0f, 2f, 4f), 0.0001f)
        assertEquals(1f, Interpolation.linear(0.5f, 0f, 0f, 2f, 4f), 0.0001f)
        assertEquals(6f, Interpolation.linear(3f, 0f, 0f, 2f, 4f), 0.0001f)
        assertEquals(-2f, Interpolation.linear(-1f, 0f, 0f, 2f, 4f), 0.0001f)
        assertEquals(5f, Interpolation.linear(1f, 0f, 5f, 2f, 5f), 0.0001f)
        assertEquals(50f, Interpolation.linear(1f, 0f, 0f, 2f, 100f), 0.0001f)
    }

    @Test
    fun cubicVector() {
        val point0 = Vector2(0f, 0f)
        val point1 = Vector2(1f, 1f)
        val point2 = Vector2(2f, 4f)
        val point3 = Vector2(3f, 9f)

        assertEquals(1f, Interpolation.cubic(1f, point0, point1, point2, point3), 0.0001f)
        assertEquals(4f, Interpolation.cubic(2f, point0, point1, point2, point3), 0.0001f)
        assertEquals(2.25f, Interpolation.cubic(1.5f, point0, point1, point2, point3), 0.0001f)
    }

    @Test
    fun cubic() {
        assertEquals(1f, Interpolation.cubic(1f, 0f, 0f, 1f, 1f, 2f, 4f, 3f, 9f), 0.0001f)
        assertEquals(4f, Interpolation.cubic(2f, 0f, 0f, 1f, 1f, 2f, 4f, 3f, 9f), 0.0001f)
        assertEquals(2.25f, Interpolation.cubic(1.5f, 0f, 0f, 1f, 1f, 2f, 4f, 3f, 9f), 0.0001f)
    }

    @Test
    fun interpolate() {
        // 3 points (quadratic interpolation)
        val xs = listOf(0f, 1f, 2f)
        val ys = listOf(0f, 1f, 4f)

        assertEquals(0f, Interpolation.interpolate(0f, xs, ys), 0.0001f)
        assertEquals(1f, Interpolation.interpolate(1f, xs, ys), 0.0001f)
        assertEquals(4f, Interpolation.interpolate(2f, xs, ys), 0.0001f)
        assertEquals(0.25f, Interpolation.interpolate(0.5f, xs, ys), 0.0001f)

        // 4 points (cubic interpolation)
        val xs2 = listOf(0f, 1f, 2f, 3f)
        val ys2 = listOf(0f, 1f, 8f, 27f)

        assertEquals(0f, Interpolation.interpolate(0f, xs2, ys2), 0.0001f)
        assertEquals(1f, Interpolation.interpolate(1f, xs2, ys2), 0.0001f)
        assertEquals(8f, Interpolation.interpolate(2f, xs2, ys2), 0.0001f)
        assertEquals(27f, Interpolation.interpolate(3f, xs2, ys2), 0.0001f)
        assertEquals(0.125f, Interpolation.interpolate(0.5f, xs2, ys2), 0.001f)
    }

    @Test
    fun getMultiplesBetweenFloat() {
        val result1 = Interpolation.getMultiplesBetween(0f, 10f, 2f)
        assertEquals(listOf(0f, 2f, 4f, 6f, 8f, 10f), result1)

        val result2 = Interpolation.getMultiplesBetween(1f, 9f, 2f)
        assertEquals(listOf(2f, 4f, 6f, 8f), result2)

        val result3 = Interpolation.getMultiplesBetween(0f, 2f, 0.5f)
        assertEquals(listOf(0f, 0.5f, 1f, 1.5f, 2f), result3)

        val result4 = Interpolation.getMultiplesBetween(-5f, 5f, 2.5f)
        assertEquals(listOf(-5f, -2.5f, 0f, 2.5f, 5f), result4)

        val result5 = Interpolation.getMultiplesBetween(0.1f, 0.4f, 1f)
        assertTrue(result5.isEmpty())

        val result6 = Interpolation.getMultiplesBetween(2f, 2f, 1f)
        assertEquals(listOf(2f), result6)
    }

    @Test
    fun getMultiplesBetweenDouble() {
        val result1 = Interpolation.getMultiplesBetween(0.0, 10.0, 2.0)
        assertEquals(listOf(0.0, 2.0, 4.0, 6.0, 8.0, 10.0), result1)

        val result2 = Interpolation.getMultiplesBetween(1.0, 9.0, 2.0)
        assertEquals(listOf(2.0, 4.0, 6.0, 8.0), result2)

        val result3 = Interpolation.getMultiplesBetween(0.0, 2.0, 0.5)
        assertEquals(listOf(0.0, 0.5, 1.0, 1.5, 2.0), result3)

        val result4 = Interpolation.getMultiplesBetween(-5.0, 5.0, 2.5)
        assertEquals(listOf(-5.0, -2.5, 0.0, 2.5, 5.0), result4)

        val result5 = Interpolation.getMultiplesBetween(0.1, 0.4, 1.0)
        assertTrue(result5.isEmpty())

        val result6 = Interpolation.getMultiplesBetween(2.0, 2.0, 1.0)
        assertEquals(listOf(2.0), result6)

        val result7 = Interpolation.getMultiplesBetween(0.0, 0.31, 0.1)
        assertEquals(4, result7.size)
        assertEquals(0.0, result7[0], 0.0001)
        assertEquals(0.1, result7[1], 0.0001)
        assertEquals(0.2, result7[2], 0.0001)
        assertEquals(0.3, result7[3], 0.0001)
    }

    @Test
    fun getIsoline() {
        val grid = listOf(
            listOf(Vector2(0f, 0f) to 0f, Vector2(1f, 0f) to 0f, Vector2(2f, 0f) to 0f),
            listOf(Vector2(0f, 1f) to 0f, Vector2(1f, 1f) to 1f, Vector2(2f, 1f) to 0f),
            listOf(Vector2(0f, 2f) to 0f, Vector2(1f, 2f) to 0f, Vector2(2f, 2f) to 0f)
        )

        val isolines = Interpolation.getIsoline(grid, 0.5f) { pct, a, b ->
            Vector2(a.x + (b.x - a.x) * pct, a.y + (b.y - a.y) * pct)
        }

        assertEquals(4, isolines.size)

        assertSegment(isolines[0], 1f, 0.5f, 0.5f, 1f)
        assertSegment(isolines[1], 1f, 0.5f, 1.5f, 1f)
        assertSegment(isolines[2], 0.5f, 1f, 1f, 1.5f)
        assertSegment(isolines[3], 1.5f, 1f, 1f, 1.5f)
    }

    private fun assertSegment(
        segment: IsolineSegment<Vector2>,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        threshold: Float = 0.001f
    ) {
        val start = segment.start
        val end = segment.end

        val match1 = (abs(start.x - x1) < threshold && abs(start.y - y1) < threshold &&
                abs(end.x - x2) < threshold && abs(end.y - y2) < threshold)

        val match2 = (abs(start.x - x2) < threshold && abs(start.y - y2) < threshold &&
                abs(end.x - x1) < threshold && abs(end.y - y1) < threshold)

        assertTrue(
            match1 || match2,
            "Expected ($x1, $y1)-($x2, $y2) but got (${start.x}, ${start.y})-(${end.x}, ${end.y})"
        )
    }
}