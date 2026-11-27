package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class OvercastPredicator : WeatherPredictor {

    private val precipitationPredictor = PrecipitationPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        val cloud = factors.cloudReadingCurrent
        val isOvercastCloud = cloud != null && cloud.value in ForecastHelper.overcastClouds
        val isCloudSky = isOvercastCloud || (cloud != null && cloud.value == null)
        return precipitationPredictor.isLikely(factors) ||
            if (isCloudSky) {
                isOvercastCloud
            } else {
                isOvercastFromPressure(factors)
            }
    }

    private fun isOvercastFromPressure(factors: SolForecastFactors): Boolean {
        val pressure = factors.pressure
        val change3h = factors.change3h
        val change12h = factors.change12h
        val monthlyPrecipitation = factors.monthlyPrecipitationMm
        val isVeryDry = monthlyPrecipitation != null && monthlyPrecipitation < VERY_DRY_MONTH_MM
        return pressure != null && change3h != null && change12h != null &&
            (
                change12h <= -FALL_HPA * factors.sensitivityScale ||
                    (!isVeryDry && pressure <= LOW_HPA && change3h <= MAX_RISE_AT_LOW_HPA * factors.sensitivityScale)
                )
    }

    private companion object {
        const val FALL_HPA = 2f
        const val LOW_HPA = 1009.144f
        const val MAX_RISE_AT_LOW_HPA = 1.5f
        const val VERY_DRY_MONTH_MM = 10f
    }
}
