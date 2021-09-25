package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.units.Reading
import java.time.Duration
import kotlin.math.absoluteValue

class HumidityWeatherForecaster(
    private val changeThreshold: Float = 3f,
    private val stormThreshold: Float = 8f,
) : IWeatherForecaster<Float> {

    override fun forecast(readings: List<Reading<Float>>): WeatherForecast {
        val tendency = getTendency(readings)

        val confidence =
            SolMath.clamp(
                SolMath.norm(
                    tendency.absoluteValue,
                    0f,
                    stormThreshold.absoluteValue
                ), 0f, 1f
            )

        return when {
            tendency >= stormThreshold -> {
                WeatherForecast(Weather.WorseningFast, confidence)
            }
            tendency >= changeThreshold -> {
                WeatherForecast(Weather.WorseningSlow, confidence)
            }
            tendency <= -stormThreshold -> {
                WeatherForecast(Weather.ImprovingFast, confidence)
            }
            tendency <= -changeThreshold -> {
                WeatherForecast(Weather.ImprovingSlow, confidence)
            }
            else -> {
                WeatherForecast(Weather.NoChange, 1 - confidence)
            }
        }

    }

    private fun getTendency(readings: List<Reading<Float>>): Float {
        val first = readings.firstOrNull() ?: return 0f
        val last = readings.lastOrNull() ?: return 0f

        val diff = last.value - first.value
        val dt = Duration.between(first.time, last.time).seconds / 3600f

        if (dt == 0f) {
            return 0f
        }

        return diff / dt
    }

}