package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZonedDateTime

internal class HarmonicLunitidalWaterLevelCalculatorTest {

    @Test
    fun calculate() {
        val calculator = HarmonicLunitidalWaterLevelCalculator(
            Duration.ofHours(2).plusMinutes(15),
            Coordinate(42.0, -72.0)
        )

        assertEquals(
            0.9f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T09:00:00-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            0.3f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T12:00:00-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            -0.9f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T15:00:00-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            -0.4f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T18:00:00-04:00[America/New_York]")),
            0.1f
        )
    }
}
