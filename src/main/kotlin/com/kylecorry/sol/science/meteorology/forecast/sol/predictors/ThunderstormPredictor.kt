package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class ThunderstormPredictor : WeatherPredictor {

    private val stormPredictor = StormPredictor()
    private val precipitationPredictor = PrecipitationPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        val dailyHigh = factors.temperatureRangeDaily?.end?.celsius()?.value ?: return false
        val monthlyPrecipitation = factors.monthlyPrecipitationMm
        return if (monthlyPrecipitation != null) {
            dailyHigh >= CLIMATE_MIN_HIGH_CELSIUS &&
                monthlyPrecipitation >= CLIMATE_MIN_MONTHLY_PRECIPITATION_MM &&
                precipitationPredictor.isLikely(factors)
        } else {
            dailyHigh >= THUNDERSTORM_MIN_HIGH_CELSIUS && stormPredictor.isLikely(factors)
        }
    }

    private companion object {
        const val THUNDERSTORM_MIN_HIGH_CELSIUS = 15f
        const val CLIMATE_MIN_HIGH_CELSIUS = 20f
        const val CLIMATE_MIN_MONTHLY_PRECIPITATION_MM = 50f
    }
}
