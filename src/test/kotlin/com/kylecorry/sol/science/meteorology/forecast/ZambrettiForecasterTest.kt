package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.science.meteorology.WeatherCondition
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Pressure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

internal class ZambrettiForecasterTest {

    private val time = Instant.ofEpochSecond(1700000000)
    private val location = Coordinate(40.0, -80.0)

    @Test
    fun steadyPressureUsesTheSteadyFormula() {
        val forecast = forecast(
            listOf(
                pressure(1025f, time.minus(Duration.ofHours(3))),
                pressure(1025f, time)
            )
        )

        // 127 - 0.12 * 1025 = 4 (clear now, precipitation later)
        assertEquals(listOf(WeatherCondition.Clear), forecast.first().conditions)
        assertTrue(forecast.last().conditions.contains(WeatherCondition.Precipitation))
    }

    @Test
    fun ignoresObservationsAfterTheForecastTime() {
        val observations = listOf(
            pressure(1025f, time.minus(Duration.ofHours(3))),
            pressure(1025f, time)
        )

        val expected = forecast(observations)
        val actual = forecast(observations + pressure(990f, time.plus(Duration.ofHours(1))))

        assertEquals(expected, actual)
    }

    private fun forecast(observations: List<WeatherObservation<*>>) =
        ZambrettiForecaster.forecast(
            observations,
            null,
            time,
            0.5f,
            2f,
            location
        )

    private fun pressure(hpa: Float, time: Instant) =
        WeatherObservation.Pressure(time, Pressure.hpa(hpa))
}
