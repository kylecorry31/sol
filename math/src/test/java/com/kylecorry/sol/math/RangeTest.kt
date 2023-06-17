package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class RangeTest {

    @Test
    fun contains() {
        assertTrue(Range(1f, 2f).contains(1.5f))
        assertTrue(Range(1f, 2f).contains(1f))
        assertTrue(Range(1f, 2f).contains(2f))
        assertFalse(Range(1f, 2f).contains(0f))
        assertFalse(Range(1f, 2f).contains(3f))
    }

    @Test
    fun clamp() {
        assertEquals(1.5f, Range(1f, 2f).clamp(1.5f))
        assertEquals(2f, Range(1f, 2f).clamp(2.5f))
        assertEquals(1f, Range(1f, 2f).clamp(0.5f))
    }

    @ParameterizedTest
    @CsvSource(
        "1, 2, 1, 2, 1, 2",
        "1, 2, 1, 1.5, 1, 1.5",
        "1, 2, 1.5, 2, 1.5, 2",
        "1, 2, 1.5, 1.5, 1.5, 1.5",
        "1, 2, 0, 1, 1, 1",
        "1, 2, 2, 3, 2, 2",
        "1, 2, 0, 3, 1, 2",
        "1, 2, 0, 0,,",
        "1, 2, 3, 3,,"
    )
    fun intersection(
        start: Float,
        end: Float,
        otherStart: Float,
        otherEnd: Float,
        expectedStart: Float?,
        expectedEnd: Float?
    ) {
        val range = Range(start, end)
        val other = Range(otherStart, otherEnd)
        val intersection = range.intersection(other)
        if (expectedStart == null) {
            assertNull(intersection)
        } else {
            assertNotNull(intersection)
            assertEquals(expectedStart, intersection!!.start)
            assertEquals(expectedEnd, intersection.end)
        }
    }
}