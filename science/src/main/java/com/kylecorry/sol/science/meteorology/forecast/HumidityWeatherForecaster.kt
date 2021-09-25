package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.units.Reading

class HumidityWeatherForecaster : IWeatherForecaster<Float> {

    override fun forecast(readings: List<Reading<Float>>): WeatherForecast {
        val last = readings.lastOrNull() ?: return WeatherForecast(Weather.Unknown, 0f)

        if (last.value == 0f || last.value.isNaN()) {
            return WeatherForecast(Weather.Unknown, 0f)
        }

        return if (last.value < 0.6f) {
            return WeatherForecast(Weather.ImprovingSlow, 1 - last.value)
        } else {
            WeatherForecast(Weather.WorseningSlow, last.value)
        }

    }
}