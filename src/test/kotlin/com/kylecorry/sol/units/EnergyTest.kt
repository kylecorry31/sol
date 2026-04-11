package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EnergyTest {

    @ParameterizedTest
    @CsvSource("1, 1, 1, 1")
    @CsvSource("1, 1, 2, 0.7375621493")
    @CsvSource("2, 2, 1, 2.7116358966")
    fun convert(value: Float, from: Int, to: Int, expected: Float) {
        val energy = Energy.from(value, EnergyUnits.entries.first { it.id == from })
        val converted = energy.convertTo(EnergyUnits.entries.first { it.id == to })
        assertEquals(expected, converted.value, 0.01f)
        assertEquals(to, converted.units.id)
    }

}