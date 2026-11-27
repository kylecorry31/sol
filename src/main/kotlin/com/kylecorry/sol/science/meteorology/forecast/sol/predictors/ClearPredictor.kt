package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class ClearPredictor : WeatherPredictor {

    private val precipitationPredictor = PrecipitationPredictor()
    private val overcastPredictor = OvercastPredicator()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        val cloud = factors.cloudReadingCurrent
        if (cloud != null && cloud.value == null) {
            return !precipitationPredictor.isLikely(factors)
        }
        if (cloud != null && cloud.value in ForecastHelper.overcastClouds) {
            return false
        }

        val pressure = factors.pressure ?: return false
        factors.change3h ?: return false
        val monthlyPrecipitation = factors.monthlyPrecipitationMm
        val isDry = monthlyPrecipitation != null && monthlyPrecipitation < DRY_MONTH_MM
        val minimumPressure = if (isDry) DRY_HIGH_HPA else HIGH_HPA
        return pressure >= minimumPressure && !overcastPredictor.isLikely(factors)
    }

    private companion object {
        const val HIGH_HPA = 1020f
        const val DRY_HIGH_HPA = 1010f
        const val DRY_MONTH_MM = 50f
    }
}
