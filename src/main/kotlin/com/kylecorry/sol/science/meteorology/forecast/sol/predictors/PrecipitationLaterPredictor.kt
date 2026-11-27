package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class PrecipitationLaterPredictor : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        val pressure = factors.pressure
        val change12h = factors.change12h
        val change24h = factors.change24h
        val monthlyPrecipitation = factors.monthlyPrecipitationMm
        val isDry = monthlyPrecipitation != null && monthlyPrecipitation < DRY_MONTH_MM
        return pressure != null && factors.change3h != null && change12h != null && change24h != null &&
            (
                (pressure <= LOW_HPA && change24h <= 0 && !isDry) ||
                    change12h <= -FALL_HPA * factors.sensitivityScale
                )
    }

    private companion object {
        const val LOW_HPA = 1012f
        const val FALL_HPA = 4f
        const val DRY_MONTH_MM = 30f
    }
}
