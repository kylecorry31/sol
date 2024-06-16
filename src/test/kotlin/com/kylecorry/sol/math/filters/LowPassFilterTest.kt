package com.kylecorry.sol.math.filters

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LowPassFilterTest {

    @Test
    fun filter() {
        val filter = LowPassFilter(0.1f, 0f)
        assertEquals(0f, filter.filter(0f))
        assertEquals(0.1f, filter.filter(1f))
        assertEquals(0.29f, filter.filter(2f))
        assertEquals(0.561f, filter.filter(3f))

        assertEquals(0.561f, filter.value)

        // Change alpha
        filter.alpha = 0f
        assertEquals(0.561f, filter.filter(4f))
    }
}