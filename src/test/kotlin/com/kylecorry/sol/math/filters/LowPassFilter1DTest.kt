package com.kylecorry.sol.math.filters

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class LowPassFilter1DTest {

    @Test
    fun filter() {
        val data = listOf(0f, 1f, 2f, 3f)
        val filter = LowPassFilter1D(0.1f)
        assertEquals(listOf(0f, 0.1f, 0.29f, 0.561f), filter.filter(data))
    }

    @Test
    fun filterEmpty() {
        val data = emptyList<Float>()
        val filter = LowPassFilter1D(0.1f)
        assertEquals(emptyList<Float>(), filter.filter(data))
    }
}