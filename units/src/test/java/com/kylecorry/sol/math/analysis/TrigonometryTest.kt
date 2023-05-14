package com.kylecorry.sol.math.analysis

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class TrigonometryTest {

    @ParameterizedTest
    @CsvSource(
        "0, 0, true, 0",
        "0, 0, false, 0",
        "0, 90, true, 270",
        "0, 90, false, 90",
        "90, 0, true, 90",
        "90, 0, false, 270",
        "90, 90, true, 0",
        "90, 90, false, 0",
        "180, 0, true, 180",
        "180, 0, false, 180",
        "180, 90, true, 90",
        "180, 90, false, 270",
        "270, 0, true, 270",
        "270, 0, false, 90",
        "270, 90, true, 180",
        "270, 90, false, 180",
        "90, 360, true, 90",
        "90, 360, false, 270",
    )
    fun remapUnitAngle(
        originalAngle: Float,
        start: Float,
        isCounterClockwise: Boolean,
        expected: Float
    ) {
        val actual = Trigonometry.remapUnitAngle(originalAngle, start, isCounterClockwise)
        assertEquals(expected, actual, 0.0001f)
    }


    @ParameterizedTest
    @CsvSource(
        "0, 0, true, 0",
        "0, 0, false, 0",
        "0, 90, true, 90",
        "0, 90, false, 90",
        "90, 0, true, 90",
        "90, 0, false, 270",
        "90, 90, true, 180",
        "90, 90, false, 0",
        "180, 0, true, 180",
        "180, 0, false, 180",
        "180, 90, true, 270",
        "180, 90, false, 270",
        "270, 0, true, 270",
        "270, 0, false, 90",
        "270, 90, true, 0",
        "270, 90, false, 180",
        "90, 360, true, 90",
        "90, 360, false, 270",
        "450, 90, true, 180",
    )
    fun toUnitAngle(
        originalAngle: Float,
        start: Float,
        isCounterClockwise: Boolean,
        expected: Float
    ) {
        val actual = Trigonometry.toUnitAngle(originalAngle, start, isCounterClockwise)
        assertEquals(expected, actual, 0.0001f)
    }

}