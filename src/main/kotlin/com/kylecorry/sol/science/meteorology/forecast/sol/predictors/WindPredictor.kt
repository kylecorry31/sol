package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class WindPredictor : WeatherPredictor {

    private val stormPredictor = StormPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        return isWindFromClouds(factors) ||
            stormPredictor.isLikely(factors) ||
            isWindFromPressure(factors)
    }

    private fun isWindFromClouds(factors: SolForecastFactors): Boolean {
        return factors.weatherFrontFromCloudsOnly == WeatherFront.Cold &&
            !factors.pressureTendency.characteristic.isRising
    }

    private fun isWindFromPressure(factors: SolForecastFactors): Boolean {
        val pressure = factors.pressure
        val change12h = factors.change12h
        return pressure != null && factors.change3h != null && change12h != null &&
            (
                (pressure <= LOW_HPA && change12h <= 0) ||
                    change12h <= -FALL_HPA * factors.sensitivityScale
                )
    }

    private companion object {
        const val LOW_HPA = 1007f
        const val FALL_HPA = 5f
    }
}
