package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class PrecipitationPredictor : WeatherPredictor {

    private val stormPredictor = StormPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        return isPrecipitationFromClouds(factors) ||
            stormPredictor.isLikely(factors) ||
            isPrecipitationFromPressure(factors)
    }

    private fun isPrecipitationFromClouds(factors: SolForecastFactors): Boolean {
        return factors.doCloudsIndicateWeatherFront && !factors.pressureTendency.characteristic.isRising
    }

    private fun isPrecipitationFromPressure(factors: SolForecastFactors): Boolean {
        val pressure = factors.pressure
        val change12h = factors.change12h
        val lowPressureFall = if (isDry(factors)) DRY_LOW_FALL_HPA * factors.sensitivityScale else 0f
        return pressure != null && factors.change3h != null && change12h != null &&
            (
                (pressure <= LOW_HPA && change12h <= -lowPressureFall) ||
                    change12h <= -FALL_HPA * factors.sensitivityScale
                )
    }

    private fun isDry(factors: SolForecastFactors): Boolean {
        val monthlyPrecipitation = factors.monthlyPrecipitationMm ?: return false
        return monthlyPrecipitation < DRY_MONTH_MM
    }

    private companion object {
        const val LOW_HPA = 1012f
        const val FALL_HPA = 4f
        const val DRY_MONTH_MM = 30f
        const val DRY_LOW_FALL_HPA = 3f
    }
}
