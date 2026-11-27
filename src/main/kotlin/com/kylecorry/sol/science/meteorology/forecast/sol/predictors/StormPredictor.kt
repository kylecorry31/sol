package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class StormPredictor : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        return isStormFromClouds(factors) || isStormFromPressure(factors)
    }

    private fun isStormFromClouds(factors: SolForecastFactors): Boolean {
        return !factors.pressureTendency.characteristic.isRising &&
            factors.weatherFrontFromCloudsOnly == WeatherFront.Cold
    }

    private fun isStormFromPressure(factors: SolForecastFactors): Boolean {
        val pressure = factors.pressure
        val change3h = factors.change3h
        val change12h = factors.change12h
        return pressure != null && change3h != null && change12h != null &&
            (
                change3h <= -factors.stormDrop3h ||
                    (pressure <= DEEP_LOW_HPA && change12h <= -DEEP_LOW_FALL_HPA)
                )
    }

    private companion object {
        const val DEEP_LOW_HPA = 1003f
        const val DEEP_LOW_FALL_HPA = 3f
    }
}
