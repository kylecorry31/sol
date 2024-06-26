package com.kylecorry.sol.math.filters

import assertk.assertThat
import assertk.assertions.isEqualTo

import org.junit.jupiter.api.Test

internal class ProximityChangeFilterGenericTest {

    @Test
    fun filter() {

        val list = listOf(1, 2, 4, 5, 6, 7, 20)
        val threshold = 2f
        val filter = ProximityChangeFilterGeneric<Int>(threshold){ a, b -> (b - a).toFloat() }
        val expected = listOf(1, 4, 6, 20)

        val actual = filter.filter(list)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun filterFill() {

        val list = listOf(1, 2, 4, 5, 6, 7, 20)
        val threshold = 2f
        val filter = ProximityChangeFilterGeneric<Int>(threshold, {previous, current -> previous + current}){ a, b -> (b - a).toFloat() }
        val expected = listOf(1, 3, 4, 9, 6, 13, 20)

        val actual = filter.filter(list)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun filterEmpty() {
        val list = emptyList<Int>()
        val threshold = 2f
        val filter = ProximityChangeFilterGeneric<Int>(threshold){ a, b -> (b - a).toFloat() }
        val expected = emptyList<Int>()

        val actual = filter.filter(list)

        assertThat(actual).isEqualTo(expected)
    }
}