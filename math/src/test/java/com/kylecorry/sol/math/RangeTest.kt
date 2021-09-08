package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

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
}