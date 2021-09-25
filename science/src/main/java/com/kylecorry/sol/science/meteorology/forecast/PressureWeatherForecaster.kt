package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.SolMath.clamp
import com.kylecorry.sol.math.SolMath.norm
import com.kylecorry.sol.science.meteorology.*
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import java.time.Duration
import kotlin.math.absoluteValue

class PressureWeatherForecaster(
    private val changeThreshold: Float = 0.5f,
    private val stormThreshold: Float = -2f,
    private val weatherService: IWeatherService = WeatherService()
) : IWeatherForecaster<Pressure> {

    override fun forecast(readings: List<Reading<Pressure>>): WeatherForecast {
        val tendency = getTendency(readings)

        val confidence =
            clamp(norm(tendency.amount.absoluteValue, 0f, stormThreshold.absoluteValue), 0f, 1f)

        val isStorm = tendency.amount <= stormThreshold

        if (isStorm) {
            return WeatherForecast(Weather.Storm, confidence)
        }

        return when (tendency.characteristic) {
            PressureCharacteristic.FallingFast -> WeatherForecast(
                Weather.WorseningFast,
                confidence
            )
            PressureCharacteristic.Falling -> WeatherForecast(
                Weather.WorseningSlow,
                confidence
            )
            PressureCharacteristic.RisingFast -> WeatherForecast(
                Weather.ImprovingFast,
                confidence
            )
            PressureCharacteristic.Rising -> WeatherForecast(
                Weather.ImprovingSlow,
                confidence
            )
            else -> WeatherForecast(
                Weather.NoChange,
                1 - confidence
            )
        }
    }

    private fun getTendency(readings: List<Reading<Pressure>>): PressureTendency {
        val last = readings.firstOrNull() ?: return PressureTendency.zero
        val current = readings.lastOrNull() ?: return PressureTendency.zero

        return weatherService.getTendency(
            last.value,
            current.value,
            Duration.between(last.time, current.time),
            changeThreshold
        )
    }

}