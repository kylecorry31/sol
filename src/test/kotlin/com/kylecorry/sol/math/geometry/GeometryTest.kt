package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class GeometryTest {

    @Test
    fun containsCircle() {
        val circle1 = Circle(Vector2(0f, 0f), 1f)

        assertTrue(Geometry.contains(circle1, Vector2(0f, 0f)))
        assertTrue(Geometry.contains(circle1, Vector2(1f, 0f)))
        assertTrue(Geometry.contains(circle1, Vector2(0f, 1f)))
        assertTrue(Geometry.contains(circle1, Vector2(0.5f, 0.5f)))
    }

    @Test
    fun getIntersectionLineCircle() {
        val line1 = Line(Vector2(0f, 0f), Vector2(3f, 0f))
        val line2 = Line(Vector2(-5f, -6f), Vector2(4f, 3f))
        val line3 = Line(Vector2(0.5f, 1f), Vector2(0.5f, -1f))
        val line4 = Line(Vector2(1f, 0f), Vector2(2f, 2f))

        // TODO: Add test where line is outside circle, but points to circle

        val circle1 = Circle(Vector2(0f, 0f), 1f)
        val circle2 = Circle(Vector2(4f, 3f), 2f)

        val intersection1 = Geometry.getIntersection(line1, circle1)
        val intersection2 = Geometry.getIntersection(line2, circle1)
        val intersection3 = Geometry.getIntersection(line2, circle2)
        val intersection4 = Geometry.getIntersection(line1, circle2)
        val intersection5 = Geometry.getIntersection(line3, circle1)
        val intersection6 = Geometry.getIntersection(line4, circle1)

        assertEquals(Line(Vector2(0f, 0f), Vector2(1f, 0f)), intersection1!!, 0.00001f)
        assertEquals(Line(Vector2(0f, -1f), Vector2(1f, 0f)), intersection2!!, 0.00001f)
        assertEquals(Line(Vector2(2.58579f, 1.58579f), Vector2(4f, 3f)), intersection3!!, 0.00001f)
        assertEquals(
            Line(Vector2(0.5f, 0.866025f), Vector2(0.5f, -0.866025f)),
            intersection5!!,
            0.00001f
        )
        assertEquals(Line(Vector2(1f, 0f), Vector2(1f, 0f)), intersection6!!, 0.00001f)
        assertNull(intersection4)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0",
        "3, 1, 3, 1",
        // Before line
        "-1, -1, 0, 0",
        // After line
        "4, 2, 3, 1",
        // On line
        "1, 0.333333, 1, 0.333333",
        // Above line
        "1, 1, 1.2, 0.4",
        // Below line
        "1, -1, 0.6, 0.2"
    )
    fun snapToLine(x: Float, y: Float, expectedX: Float, expectedY: Float) {
        val x1 = 0f
        val y1 = 0f
        val x2 = 3f
        val y2 = 1f

        // Raw
        assertEquals(Vector2(expectedX, expectedY), Geometry.snapToLine(x, y, x1, y1, x2, y2), 0.00001f)

        // Objects
        assertEquals(
            Vector2(expectedX, expectedY),
            Geometry.snapToLine(Vector2(x, y), Line(Vector2(x1, y1), Vector2(x2, y2))),
            0.00001f
        )
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0",
        "3, 1, 3, 1, 2",
        // Before line
        "-1, -1, 0, 0, 0",
        // After line
        "4, 2, 3, 1, 2",
        // On line
        "1, 0.333333, 1, 0.333333, 0.666667",
        // Above line
        "1, 1, 1.2, 0.4, 0.8",
        // Below line
        "1, -1, 0.6, 0.2, 0.4"
    )
    fun snapTo3DLine(x: Float, y: Float, expectedX: Float, expectedY: Float, expectedZ: Float) {
        val x1 = 0f
        val y1 = 0f
        val z1 = 0f
        val x2 = 3f
        val y2 = 1f
        val z2 = 2f

        // Raw
        assertEquals(Vector3(expectedX, expectedY, expectedZ), Geometry.snapTo3DLine(Vector2(x, y), x1, y1, z1, x2, y2, z2), 0.00001f)

        // Objects
        assertEquals(
            Vector3(expectedX, expectedY, expectedZ),
            Geometry.snapTo3DLine(Vector2(x, y), x1, y1, z1, x2, y2, z2),
            0.00001f
        )
    }

    private fun assertEquals(expected: Line, actual: Line, delta: Float = 0f) {
        assertEquals(expected.start, actual.start, delta)
        assertEquals(expected.end, actual.end, delta)
    }

    private fun assertEquals(expected: Vector2, actual: Vector2, delta: Float = 0f) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
    }

    private fun assertEquals(expected: Vector3, actual: Vector3, delta: Float = 0f) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
        assertEquals(expected.z, actual.z, delta)
    }

}