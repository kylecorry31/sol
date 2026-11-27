package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class ThunderstormPredictor : WeatherPredictor {

    private val stormPredictor = StormPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        val dailyHigh = factors.temperatureRangeDaily?.end?.celsius()?.value ?: return false
        return dailyHigh >= THUNDERSTORM_MIN_HIGH_CELSIUS && stormPredictor.isLikely(factors)
    }

    private companion object {
        const val THUNDERSTORM_MIN_HIGH_CELSIUS = 15f
    }
}
