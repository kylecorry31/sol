package com.kylecorry.sol.math.filters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComplementaryFilterTest {

    @Test
    fun complementaryFilter(){
        val filter = ComplementaryFilter(listOf(0.5f, 0.2f, 0.3f), 1f)
        assertEquals(1f, filter.value, 0.001f)

        filter.filter(listOf(1f, 1f, 1f))
        assertEquals(1f, filter.value, 0.001f)

        filter.filter(listOf(2f, 3f, 4f))
        assertEquals(2.8f, filter.value, 0.001f)

        // Change the weights
        filter.weights = listOf(0.1f, 0.2f, 0.7f)
        filter.filter(listOf(2f, 3f, 4f))
        assertEquals(3.6f, filter.value, 0.001f)

        // Weights that don't sum to 1
        filter.weights = listOf(0.2f, 0.2f, 0.2f)
        filter.filter(listOf(3f, 6f, 12f))
        assertEquals(7f, filter.value, 0.001f)
    }

}