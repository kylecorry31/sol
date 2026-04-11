package com.kylecorry.sol.shared

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.assertThrows

class GuardsTest {

    @Test
    fun isPositive() {
        assertThrows<IllegalArgumentException> {
            Guards.isPositive(-1)
        }
        assertThrows<IllegalArgumentException> {
            Guards.isPositive(0)
        }
        Guards.isPositive(1)
    }

    @Test
    fun isNotEmpty() {
        assertThrows<IllegalArgumentException> {
            Guards.isNotEmpty(listOf<Int>())
        }
        Guards.isNotEmpty(listOf(1))
    }

    @Test
    fun areSameSize() {
        assertThrows<IllegalArgumentException> {
            Guards.areSameSize(listOf(1), listOf(1, 2))
        }
        Guards.areSameSize(listOf(1), listOf(2))
    }
}