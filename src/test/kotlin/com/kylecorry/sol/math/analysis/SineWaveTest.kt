package com.kylecorry.sol.math.analysis

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SineWaveTest {

    @ParameterizedTest
    @CsvSource(
        "1, 1, 0, 0, 0, 0",
        "1, 1, 0, 0, 1, 0.8415",
        "1, 1, 0, 0, 2, 0.9093",
        "1, 1, 0, 0, 3, 0.1411",
        "1, 1, 0, 0, 4, -0.7568",
        "1, 1, 0, 0, 5, -0.9589",
        "1, 1, 0, 0, 6, -0.2794",
        "1, 1, 0, 0, 7, 0.657",
        "1, 1, 0, 0, 8, 0.9894",
        "1, 1, 0, 0, 9, 0.4121",
        "1, 1, 0, 0, 10, -0.544",
        "1, 1, 1, 2, 0, 1.1585",
        "2, 4, 1, 1, 0, 2.5136",
    )
    fun calculate(amplitude: Float, frequency: Float, horizontalShift: Float, verticalShift: Float, x: Float, expected: Float) {
        val wave = SineWave(amplitude, frequency, horizontalShift, verticalShift)
        val actual = wave.calculate(x)
        assertEquals(expected, actual, 0.0001f)
    }
}