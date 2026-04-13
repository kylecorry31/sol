package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors
import com.kylecorry.sol.units.Temperature

internal class ThunderstormPredictor(pressureTendencyStormThreshold: Float) : WeatherPredictor {

    private val stormPredictor = StormPredictor(pressureTendencyStormThreshold)

    override fun isLikely(factors: SolForecastFactors): Boolean {
        if (!stormPredictor.isLikely(factors)) {
            return false
        }

        val temp = factors.temperatureRangeDaily?.end?.celsius() ?: Temperature.zero
        return factors.weatherFront == WeatherFront.Cold && temp > TEMPERATURE_THUNDERSTORM_MIN
    }

    companion object {
        private val TEMPERATURE_THUNDERSTORM_MIN = Temperature.fahrenheit(55f).celsius()
    }
}