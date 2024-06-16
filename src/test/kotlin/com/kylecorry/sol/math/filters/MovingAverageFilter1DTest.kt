package com.kylecorry.sol.math.filters

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class MovingAverageFilter1DTest {

    @Test
    fun filter() {
        val data = listOf(1f, 2f, 2f, 1f, 8f)
        val filter = MovingAverageFilter1D(3)

        val filtered = filter.filter(data)
        assertEquals(listOf(1f, 1.5f, 5/3f, 5/3f, 11/3f), filtered)
    }

    @Test
    fun filterEmpty() {
        val data = listOf<Float>()
        val filter = MovingAverageFilter1D(3)

        val filtered = filter.filter(data)
        assertEquals(listOf<Float>(), filtered)
    }
}