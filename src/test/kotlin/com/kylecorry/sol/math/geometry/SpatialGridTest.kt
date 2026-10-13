package com.kylecorry.sol.math.geometry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.random.Random

class SpatialGridTest {

    @Test
    fun candidatesIncludeEveryPointWithinTheCellSizeInEachCoordinate() {
        val random = Random(1)
        val points = List(500) { listOf(random.nextFloat() * 10, random.nextFloat() * 10) }
        val grid = SpatialGrid(points, 0.5f)

        for (point in points.take(50)) {
            val expected = points.indices.filter { i ->
                points[i].indices.all { abs(points[i][it] - point[it]) <= 0.5f }
            }
            val candidates = grid.getCandidates(point)
            assertTrue(candidates.containsAll(expected))
        }
    }

    @Test
    fun candidatesExcludeDistantPoints() {
        val points = listOf(listOf(0f, 0f), listOf(0.1f, 0.1f), listOf(50f, 50f))
        val grid = SpatialGrid(points, 1f)

        assertEquals(setOf(0, 1), grid.getCandidates(listOf(0f, 0f)).toSet())
    }

    @Test
    fun candidatesHaveNoDuplicates() {
        val points = List(100) { listOf(it * 0.1f, 0f, it * 0.05f) }
        val grid = SpatialGrid(points, 1f)

        val candidates = grid.getCandidates(points[50])

        assertEquals(candidates.size, candidates.toSet().size)
    }

    @Test
    fun candidatesCanBeWrittenToAnArray() {
        val points = listOf(listOf(0f), listOf(0.5f), listOf(10f))
        val grid = SpatialGrid(points, 1f)
        val out = IntArray(grid.size)

        val count = grid.getCandidates(listOf(0f), out)

        assertEquals(setOf(0, 1), out.take(count).toSet())
    }

    @Test
    fun candidatesAcrossNegativeCoordinates() {
        val points = listOf(listOf(-0.2f), listOf(0.2f), listOf(-5f))
        val grid = SpatialGrid(points, 1f)

        assertEquals(setOf(0, 1), grid.getCandidates(listOf(0f)).toSet())
    }

    @Test
    fun emptyGridHasNoCandidates() {
        val grid = SpatialGrid(emptyList(), 1f)

        assertTrue(grid.getCandidates(listOf(1f, 2f)).isEmpty())
    }

    @Test
    fun cellSizeMustBePositive() {
        assertThrows(IllegalArgumentException::class.java) { SpatialGrid(listOf(listOf(0f)), 0f) }
    }
}
