package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.meteorology.*
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.time.Time.plusHours
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Quantity
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.hpa
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

internal object ZambrettiForecaster : Forecaster {

    private val conditions = mapOf(
        1 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        2 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        3 to Conditions(listOf(WeatherCondition.Clear), listOf()), // Less settled?
        4 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Precipitation)),
        5 to Conditions(
            listOf(WeatherCondition.Precipitation),
            listOf(WeatherCondition.Precipitation)
        ), // More unsettled?
        6 to Conditions(
            listOf(WeatherCondition.Precipitation),
            listOf(WeatherCondition.Precipitation)
        ), // Unsettled (current)
        7 to Conditions(
            listOf(WeatherCondition.Precipitation),
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind)
        ), // Worse later
        8 to Conditions(
            listOf(WeatherCondition.Precipitation),
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind)
        ), // Becoming very unsettled
        9 to Conditions(
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind),
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind)
        ), // Very unsettled (current)
        10 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        11 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        12 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Precipitation)), // Possibly showers
        13 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Precipitation)), // Showers likely
        14 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Precipitation)),
        15 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Precipitation)),
        16 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Precipitation)),
        17 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Precipitation)),
        18 to Conditions(
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind),
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind)
        ),
        19 to Conditions(
            listOf(WeatherCondition.Storm, WeatherCondition.Precipitation, WeatherCondition.Wind),
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind)
        ),
        20 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        21 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        22 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        23 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Clear)),
        24 to Conditions(listOf(WeatherCondition.Clear), listOf(WeatherCondition.Precipitation)),
        25 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Clear)),
        26 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Clear)),
        27 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Clear)),
        28 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Clear)),
        29 to Conditions(listOf(WeatherCondition.Precipitation), listOf(WeatherCondition.Clear)),
        30 to Conditions(listOf(WeatherCondition.Precipitation, WeatherCondition.Wind), listOf(WeatherCondition.Clear)),
        31 to Conditions(
            listOf(WeatherCondition.Storm, WeatherCondition.Precipitation, WeatherCondition.Wind),
            listOf(WeatherCondition.Clear)
        ),
        32 to Conditions(
            listOf(WeatherCondition.Storm, WeatherCondition.Precipitation, WeatherCondition.Wind),
            listOf(WeatherCondition.Precipitation, WeatherCondition.Wind)
        )
    )

    override fun forecast(
        observations: List<WeatherObservation<*>>,
        dailyTemperatureRange: Range<Temperature>?,
        time: Instant,
        pressureChangeThreshold: Float,
        pressureStormChangeThreshold: Float,
        location: Coordinate
    ): List<WeatherForecast> {
        val pressures = observations
            .filterIsInstance<WeatherObservation.Pressure>()
            .map { it.asReading() }

        val pressure = pressures.lastOrNull()?.value ?: return emptyList()
        val tendency = ForecastHelper.getTendency(pressures, pressureChangeThreshold)
        val windDirection = observations
            .filterIsInstance<WeatherObservation.WindDirection>()
            .filter { it.time >= time.minus(Duration.ofHours(3)) }
            .map { it.value }
            .lastOrNull()

        return forecast(
            location,
            pressure,
            tendency,
            windDirection,
            dailyTemperatureRange,
            time.toZonedDateTime(),
            pressureChangeThreshold
        )

    }

    private fun forecast(
        location: Coordinate,
        pressure: Quantity<Pressure>,
        tendency: PressureTendency,
        windDirection: Bearing?,
        dailyTemperatureRange: Range<Temperature>?,
        time: ZonedDateTime,
        changeThreshold: Float
    ): List<WeatherForecast> {

        val hpa = pressure.hpa().amount

        // TODO: Modify this to handle the southern hemisphere wind direction (and maybe factor in the prevailing wind direction)

        val season = Astronomy.getSeason(location, time)


        val z = if (tendency.amount < changeThreshold) {
            144 - 0.13 * hpa
        } else if (tendency.amount > 0) {
            185 - 0.16 * hpa
        } else {
            127 - 0.12 * hpa
        }

        val windDirectionDegrees = windDirection?.value
        val windAdjustment = when {
            windDirectionDegrees == null -> 0
            windDirectionDegrees > 225 && windDirectionDegrees <= 315 -> 1
            windDirectionDegrees > 135 && windDirectionDegrees <= 225 -> 2
            windDirectionDegrees > 45 && windDirectionDegrees <= 135 -> 1
            else -> 0
        }

        val seasonAdjustment = when {
            season == Season.Winter && tendency.amount <= -changeThreshold -> -1
            season == Season.Summer && tendency.amount >= changeThreshold -> 1
            else -> 0
        }

        var zAdjusted = z + windAdjustment + seasonAdjustment

        if (zAdjusted < 1) {
            zAdjusted = 1.0
        } else if (zAdjusted > 32) {
            zAdjusted = 32.0
        }

        val current = WeatherForecast(
            time.toInstant(),
            getCurrentConditions(zAdjusted, dailyTemperatureRange),
            system = if (Meteorology.isHighPressure(pressure)) {
                PressureSystem.High
            } else if (Meteorology.isLowPressure(pressure)) {
                PressureSystem.Low
            } else {
                null
            },
            tendency = tendency
        )

        val later = WeatherForecast(
            time.toInstant().plusHours(12),
            getLaterConditions(zAdjusted, dailyTemperatureRange)
        )

        return listOf(current, later)
    }

    private fun getCurrentConditions(
        z: Double,
        dailyTemperatureRange: Range<Temperature>?,
    ): List<WeatherCondition> {
        return ForecastHelper.addSecondaryConditions(
            conditions[z.toInt()]?.current ?: emptyList(),
            dailyTemperatureRange
        )
    }

    private fun getLaterConditions(
        z: Double,
        dailyTemperatureRange: Range<Temperature>?,
    ): List<WeatherCondition> {
        return ForecastHelper.addSecondaryConditions(conditions[z.toInt()]?.later ?: emptyList(), dailyTemperatureRange)
    }


    private data class Conditions(val current: List<WeatherCondition>, val later: List<WeatherCondition>)

}