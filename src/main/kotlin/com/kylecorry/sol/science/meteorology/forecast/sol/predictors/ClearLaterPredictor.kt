package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class ClearLaterPredictor : WeatherPredictor {

    private val overcastLaterPredictor = OvercastLaterPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        return !overcastLaterPredictor.isLikely(factors) &&
            (isRising(factors) || factors.weatherFrontFromCloudsOnly == WeatherFront.Cold)
    }

    private fun isRising(factors: SolForecastFactors): Boolean {
        val change12h = factors.change12h
        return factors.change3h != null && change12h != null && change12h >= RISE_HPA * factors.sensitivityScale
    }

    private companion object {
        const val RISE_HPA = 1.7f
    }
}
