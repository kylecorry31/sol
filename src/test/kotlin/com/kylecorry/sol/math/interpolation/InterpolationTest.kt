package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
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
        assertEquals(listOf(0f, 2f, 4f, 6f, 8f, 10f), result1.toList())

        val result2 = Interpolation.getMultiplesBetween(1f, 9f, 2f)
        assertEquals(listOf(2f, 4f, 6f, 8f), result2.toList())

        val result3 = Interpolation.getMultiplesBetween(0f, 2f, 0.5f)
        assertEquals(listOf(0f, 0.5f, 1f, 1.5f, 2f), result3.toList())

        val result4 = Interpolation.getMultiplesBetween(-5f, 5f, 2.5f)
        assertEquals(listOf(-5f, -2.5f, 0f, 2.5f, 5f), result4.toList())

        val result5 = Interpolation.getMultiplesBetween(0.1f, 0.4f, 1f)
        assertTrue(result5.isEmpty())

        val result6 = Interpolation.getMultiplesBetween(2f, 2f, 1f)
        assertEquals(listOf(2f), result6.toList())
    }

    @Test
    fun getMultiplesBetweenDouble() {
        val result1 = Interpolation.getMultiplesBetween(0.0, 10.0, 2.0)
        assertEquals(listOf(0.0, 2.0, 4.0, 6.0, 8.0, 10.0), result1.toList())

        val result2 = Interpolation.getMultiplesBetween(1.0, 9.0, 2.0)
        assertEquals(listOf(2.0, 4.0, 6.0, 8.0), result2.toList())

        val result3 = Interpolation.getMultiplesBetween(0.0, 2.0, 0.5)
        assertEquals(listOf(0.0, 0.5, 1.0, 1.5, 2.0), result3.toList())

        val result4 = Interpolation.getMultiplesBetween(-5.0, 5.0, 2.5)
        assertEquals(listOf(-5.0, -2.5, 0.0, 2.5, 5.0), result4.toList())

        val result5 = Interpolation.getMultiplesBetween(0.1, 0.4, 1.0)
        assertTrue(result5.isEmpty())

        val result6 = Interpolation.getMultiplesBetween(2.0, 2.0, 1.0)
        assertEquals(listOf(2.0), result6.toList())

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

    @Test
    fun interpolateCatmullRom() {
        assertEquals(
            0.876125,
            Interpolation.catmullRom(0.18125, 0.884226, 0.877366, 0.870531),
            0.0000005
        )
    }

    @ParameterizedTest
    @MethodSource("provideNorm")
    fun normDouble(value: Double, min: Double, max: Double, shouldClamp: Boolean, expected: Double) {
        val actual = Interpolation.norm(value, min, max, shouldClamp)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideNorm")
    fun normFloat(value: Double, min: Double, max: Double, shouldClamp: Boolean, expected: Double) {
        val actual = Interpolation.norm(value.toFloat(), min.toFloat(), max.toFloat(), shouldClamp)
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideLerp")
    fun lerpDouble(value: Double, min: Double, max: Double, shouldClamp: Boolean, expected: Double) {
        val actual = Interpolation.lerp(value, min, max, shouldClamp)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideLerp")
    fun lerpFloat(value: Double, min: Double, max: Double, shouldClamp: Boolean, expected: Double) {
        val actual = Interpolation.lerp(value.toFloat(), min.toFloat(), max.toFloat(), shouldClamp)
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideMap")
    fun mapDouble(
        value: Double,
        min: Double,
        max: Double,
        newMin: Double,
        newMax: Double,
        shouldClamp: Boolean,
        expected: Double
    ) {
        val actual = Interpolation.map(value, min, max, newMin, newMax, shouldClamp)
        assertEquals(expected, actual, 0.00001)
    }

    @ParameterizedTest
    @MethodSource("provideMap")
    fun mapFloat(
        value: Double,
        min: Double,
        max: Double,
        newMin: Double,
        newMax: Double,
        shouldClamp: Boolean,
        expected: Double
    ) {
        val actual = Interpolation.map(
            value.toFloat(),
            min.toFloat(),
            max.toFloat(),
            newMin.toFloat(),
            newMax.toFloat(),
            shouldClamp
        )
        assertEquals(expected.toFloat(), actual, 0.00001f)
    }

    companion object {
        @JvmStatic
        fun provideNorm(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, false, 0.1),
                Arguments.of(0.0, 0.0, 1.0, false, 0.0),
                Arguments.of(1.0, 0.0, 1.0, false, 1.0),
                Arguments.of(1.2, 0.0, 1.0, false, 1.2),
                Arguments.of(-0.1, 0.0, 1.0, false, -0.1),
                Arguments.of(4.0, 2.0, 6.0, false, 0.5),
                Arguments.of(1.0, 2.0, 6.0, false, -0.25),
                Arguments.of(6.0, 2.0, 6.0, false, 1.0),
                Arguments.of(2.0, 2.0, 6.0, false, 0.0),
                Arguments.of(-1.0, 0.0, 1.0, true, 0.0),
                Arguments.of(0.5, 0.0, 1.0, true, 0.5),
                Arguments.of(2.0, 0.0, 1.0, true, 1.0),
            )
        }

        @JvmStatic
        fun provideLerp(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, false, 0.1),
                Arguments.of(0.0, 0.0, 1.0, false, 0.0),
                Arguments.of(1.0, 0.0, 1.0, false, 1.0),
                Arguments.of(1.2, 0.0, 1.0, false, 1.2),
                Arguments.of(-0.1, 0.0, 1.0, false, -0.1),
                Arguments.of(0.5, 2.0, 6.0, false, 4.0),
                Arguments.of(-0.25, 2.0, 6.0, false, 1.0),
                Arguments.of(1.0, 2.0, 6.0, false, 6.0),
                Arguments.of(0.0, 2.0, 6.0, false, 2.0),
                Arguments.of(-1.0, 0.0, 1.0, true, 0.0),
                Arguments.of(0.5, 0.0, 1.0, true, 0.5),
                Arguments.of(2.0, 0.0, 1.0, true, 1.0),
            )
        }

        @JvmStatic
        fun provideMap(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.1, 0.0, 1.0, 2.0, 4.0, false, 2.2),
                Arguments.of(0.0, 0.0, 1.0, 2.0, 4.0, false, 2.0),
                Arguments.of(1.0, 0.0, 1.0, 2.0, 4.0, false, 4.0),
                Arguments.of(1.2, 0.0, 1.0, 2.0, 4.0, false, 4.4),
                Arguments.of(-0.1, 0.0, 1.0, 2.0, 4.0, false, 1.8),
                Arguments.of(4.0, 2.0, 6.0, 0.0, 4.0, false, 2.0),
                Arguments.of(1.0, 2.0, 6.0, 0.0, 4.0, false, -1.0),
                Arguments.of(6.0, 2.0, 6.0, 0.0, 4.0, false, 4.0),
                Arguments.of(2.0, 2.0, 6.0, 0.0, 4.0, false, 0.0),
                Arguments.of(-1.0, 0.0, 1.0, 2.0, 4.0, true, 2.0),
                Arguments.of(0.5, 0.0, 1.0, 2.0, 4.0, true, 3.0),
                Arguments.of(2.0, 0.0, 1.0, 2.0, 4.0, true, 4.0),
            )
        }
    }
}