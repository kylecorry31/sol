package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class GeometryServiceTest {

    private val service = GeometryService()

    @Test
    fun containsCircle() {
        val circle1 = Circle(Vector2(0f, 0f), 1f)

        assertTrue(service.contains(circle1, Vector2(0f, 0f)))
        assertTrue(service.contains(circle1, Vector2(1f, 0f)))
        assertTrue(service.contains(circle1, Vector2(0f, 1f)))
        assertTrue(service.contains(circle1, Vector2(0.5f, 0.5f)))
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

        val intersection1 = service.getIntersection(line1, circle1)
        val intersection2 = service.getIntersection(line2, circle1)
        val intersection3 = service.getIntersection(line2, circle2)
        val intersection4 = service.getIntersection(line1, circle2)
        val intersection5 = service.getIntersection(line3, circle1)
        val intersection6 = service.getIntersection(line4, circle1)

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

    private fun assertEquals(expected: Line, actual: Line, delta: Float = 0f) {
        assertEquals(expected.start, actual.start, delta)
        assertEquals(expected.end, actual.end, delta)
    }

    private fun assertEquals(expected: Vector2, actual: Vector2, delta: Float = 0f) {
        assertEquals(expected.x, actual.x, delta)
        assertEquals(expected.y, actual.y, delta)
    }

}