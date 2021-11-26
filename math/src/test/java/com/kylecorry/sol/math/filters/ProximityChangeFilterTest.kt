package com.kylecorry.sol.math.filters

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class ProximityChangeFilterTest {

    @Test
    fun filter() {

        val list = listOf(1, 2, 4, 5, 6, 7, 20)
        val threshold = 2f
        val filter = ProximityChangeFilter<Int>(threshold){ a, b -> (b - a).toFloat() }
        val expected = listOf(1, 4, 6, 20)

        val actual = filter.filter(list)

        assertThat(actual).isEqualTo(expected)
    }
}