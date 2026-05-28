package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.tests.assertVector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PolygonTest {

    @Test
    fun areaIsPositiveForClockwiseAndCounterClockwisePolygons() {
        val counterClockwise = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(4f, 0f),
                Vector2(4f, 3f),
                Vector2(0f, 3f)
            )
        )
        val clockwise = Polygon(counterClockwise.vertices.reversed())

        assertEquals(12f, counterClockwise.area(), 0.00001f)
        assertEquals(12f, clockwise.area(), 0.00001f)
    }

    @Test
    fun signedAreaTracksVertexWinding() {
        val counterClockwise = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(4f, 0f),
                Vector2(4f, 3f),
                Vector2(0f, 3f)
            )
        )
        val clockwise = Polygon(counterClockwise.vertices.reversed())

        assertEquals(12f, counterClockwise.signedArea(), 0.00001f)
        assertEquals(-12f, clockwise.signedArea(), 0.00001f)
    }

    @Test
    fun centroidOfRectangle() {
        val polygon = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(4f, 0f),
                Vector2(4f, 3f),
                Vector2(0f, 3f)
            )
        )

        assertVector(Vector2(2f, 1.5f), polygon.centroid(), 0.00001f)
    }

    @Test
    fun centroidOfConcavePolygon() {
        val polygon = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(4f, 0f),
                Vector2(4f, 4f),
                Vector2(2f, 2f),
                Vector2(0f, 4f)
            )
        )

        assertVector(Vector2(2f, 1.5555556f), polygon.centroid(), 0.00001f)
    }

    @Test
    fun centroidOfZeroAreaPolygonIsFirstVertex() {
        val polygon = Polygon(
            listOf(
                Vector2(1f, 2f),
                Vector2(3f, 4f),
                Vector2(5f, 6f)
            )
        )

        assertVector(Vector2(1f, 2f), polygon.centroid(), 0.00001f)
    }

    @Test
    fun centroidOfEmptyPolygonIsZero() {
        val polygon = Polygon(emptyList())

        assertVector(Vector2.zero, polygon.centroid(), 0.00001f)
    }

    @Test
    fun isConvexReturnsTrueForConvexPolygon() {
        val polygon = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(4f, 0f),
                Vector2(4f, 3f),
                Vector2(0f, 3f)
            )
        )

        assertTrue(polygon.isConvex())
        assertTrue(Polygon(polygon.vertices.reversed()).isConvex())
    }

    @Test
    fun isConvexReturnsFalseForConcavePolygon() {
        val polygon = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(4f, 0f),
                Vector2(2f, 2f),
                Vector2(4f, 4f),
                Vector2(0f, 4f)
            )
        )

        assertFalse(polygon.isConvex())
    }

    @Test
    fun isConvexReturnsFalseForCollinearVertices() {
        val polygon = Polygon(
            listOf(
                Vector2(0f, 0f),
                Vector2(2f, 0f),
                Vector2(4f, 0f),
                Vector2(4f, 3f),
                Vector2(0f, 3f)
            )
        )

        assertFalse(polygon.isConvex())
    }
}
