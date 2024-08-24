package com.kylecorry.sol.math.analysis

import org.junit.jupiter.api.Assertions.assertEquals
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

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0",
        "0, 1, 1, 1",
        "1, 0, -1, 1",
        "1, 1, 0, 2",
    )
    fun getRangeY(amplitude: Float, verticalOffset: Float, expectedMin: Float, expectedMax: Float) {
        val wave = CosineWave(amplitude, 1f, 0f, verticalOffset)
        val range = Trigonometry.getRangeY(wave)
        assertEquals(expectedMin, range.start, 0.0001f)
        assertEquals(expectedMax, range.end, 0.0001f)
    }

    @Test
    fun getCombinationRangeY() {
        val waves = listOf(
            CosineWave(1f, 1f, 0f, 0f),
            CosineWave(2f, 3f, 1f, 1f),
            CosineWave(3f, 2f, 0f, 0f)
        )

        val range = Trigonometry.getCombinationRangeY(waves)

        assertEquals(-5f, range.start, 0.0001f)
        assertEquals(7f, range.end, 0.0001f)
    }

}