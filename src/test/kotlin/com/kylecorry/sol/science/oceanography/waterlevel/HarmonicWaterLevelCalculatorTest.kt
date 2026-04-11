package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.science.oceanography.TidalHarmonic
import com.kylecorry.sol.science.oceanography.TideConstituent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class HarmonicWaterLevelCalculatorTest {

    @Test
    fun calculate() {
        val harmonics = listOf(
            TidalHarmonic(TideConstituent.M2, 1.66f, 2.3f),
            TidalHarmonic(TideConstituent.S2, 0.35f, 25f),
            TidalHarmonic(TideConstituent.N2, 0.41f, 345.8f),
            TidalHarmonic(TideConstituent.K1, 0.2f, 166.1f),
            TidalHarmonic(TideConstituent.O1, 0.15f, 202f),
            TidalHarmonic(TideConstituent.P1, 0.07f, 176.6f),
            TidalHarmonic(TideConstituent.M4, 0.19f, 35.8f),
            TidalHarmonic(TideConstituent.K2, 0.1f, 21.7f),
            TidalHarmonic(TideConstituent.L2, 0.04f, 349.9f),
            TidalHarmonic(TideConstituent.MS4, 0.05f, 106.4f),
            TidalHarmonic(TideConstituent.Z0, 0f, 0f)
        )

        val zone = ZoneId.of("America/New_York")
        val date = LocalDate.of(2021, 12, 22)

        val calculator = HarmonicWaterLevelCalculator(harmonics)

        val delta = 0.35f
        assertEquals(
            calculator.calculate(
                ZonedDateTime.of(date, LocalTime.of(2, 35), zone)
            ), -1.69f, delta
        )
        assertEquals(
            calculator.calculate(
                ZonedDateTime.of(date, LocalTime.of(9, 27), zone)
            ), 1.59f, delta
        )
        assertEquals(
            calculator.calculate(
                ZonedDateTime.of(date, LocalTime.of(15, 27), zone)
            ), -1.59f, delta
        )
        assertEquals(
            calculator.calculate(
                ZonedDateTime.of(date, LocalTime.of(22, 0), zone)
            ), 1.13f, delta
        )
    }
}