package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.WeatherForecast
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Temperature
import java.time.Instant

internal interface Forecaster {

    fun forecast(
        observations: List<WeatherObservation<*>>,
        dailyTemperatureRange: Range<Temperature>? = null,
        time: Instant = Instant.now(),
        pressureChangeThreshold: Float = 1.5f,
        pressureStormChangeThreshold: Float = 2f,
        location: Coordinate = Coordinate.zero
    ): List<WeatherForecast>

}