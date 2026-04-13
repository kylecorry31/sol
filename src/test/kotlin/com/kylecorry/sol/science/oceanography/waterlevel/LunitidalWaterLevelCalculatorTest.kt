package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZonedDateTime

internal class LunitidalWaterLevelCalculatorTest {

    @Test
    fun calculateWithHighLunitidalInterval() {
        val location = Coordinate(42.0, -72.0)
        val range = Range(0f, 12f)
        val calculator = LunitidalWaterLevelCalculator(
            Duration.ofHours(2),
            location,
            waterLevelRange = range
        )

        assertEquals(
            12.0f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T09:20:14.944-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            0.0f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T15:27:34.942-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            6.0f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T12:23:54.943-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            10.2f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T10:51:44.944-04:00[America/New_York]")),
            0.1f
        )
    }

    @Test
    fun calculateWithLowLunitidalInterval() {
        val location = Coordinate(42.0, -72.0)
        val range = Range(0f, 12f)
        val calculator = LunitidalWaterLevelCalculator(
            Duration.ofHours(2),
            location,
            lowLunitidalInterval = Duration.ofHours(8),
            waterLevelRange = range
        )

        assertEquals(
            12.0f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T09:12:34.011-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            0.0f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T15:22:34.009-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            6.0f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T12:17:34.010-04:00[America/New_York]")),
            0.1f
        )
        assertEquals(
            10.3f,
            calculator.calculate(ZonedDateTime.parse("2026-04-10T10:45:04.011-04:00[America/New_York]")),
            0.1f
        )
    }

    @Test
    fun constructorRejectsDescendingWaterLevelRange() {
        assertThrows(IllegalArgumentException::class.java) {
            LunitidalWaterLevelCalculator(Duration.ofHours(2), waterLevelRange = Range(12f, 0f))
        }
    }
}
