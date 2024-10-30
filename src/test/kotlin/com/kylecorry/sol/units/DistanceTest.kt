package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DistanceTest {

    @ParameterizedTest
    @CsvSource(
        "1, 1, 6, 0.00001",
        "1, 1, 9, 10"
    )
    fun convertTo(fromId: Int, fromValue: Float, toId: Int, expected: Float) {
        val distance = Distance(fromValue, DistanceUnits.entries.first { it.id == fromId })
        val converted = distance.convertTo(DistanceUnits.entries.first { it.id == toId })
        assertEquals(expected, converted.distance, 0.00001f)
    }
}