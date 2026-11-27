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
        return pressure >= HIGH_HPA && !overcastPredictor.isLikely(factors)
    }

    private companion object {
        const val HIGH_HPA = 1020f
    }
}
