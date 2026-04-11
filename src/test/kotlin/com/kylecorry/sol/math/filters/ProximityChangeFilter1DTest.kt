package com.kylecorry.sol.math.filters

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class ProximityChangeFilter1DTest {

    @Test
    fun filter() {
        val list = listOf(1f, 2f, 4f, 5f, 6f, 7f, 20f)
        val threshold = 2f
        val filter = ProximityChangeFilter1D(threshold)
        val expected = listOf(1f, 1f, 4f, 4f, 6f, 6f, 20f)

        val actual = filter.filter(list)

        assertThat(actual).isEqualTo(expected)
    }
}