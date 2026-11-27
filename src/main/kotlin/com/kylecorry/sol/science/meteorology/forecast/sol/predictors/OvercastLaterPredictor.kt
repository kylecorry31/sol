package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class OvercastLaterPredictor : WeatherPredictor {

    private val precipitationLaterPredictor = PrecipitationLaterPredictor()

    override fun isLikely(factors: SolForecastFactors): Boolean {
        return precipitationLaterPredictor.isLikely(factors) ||
            isUnsettledAndFalling(factors) ||
            factors.weatherFrontFromCloudsOnly == WeatherFront.Warm
    }

    private fun isUnsettledAndFalling(factors: SolForecastFactors): Boolean {
        val range48h = factors.range48h
        val change24h = factors.change24h
        return factors.change3h != null && range48h != null && change24h != null &&
            range48h >= UNSETTLED_RANGE_HPA && change24h <= -FALL_HPA * factors.sensitivityScale
    }

    private companion object {
        const val UNSETTLED_RANGE_HPA = 6.8f
        const val FALL_HPA = 0.7f
    }
}
