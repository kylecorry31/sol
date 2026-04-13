package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal interface WeatherPredictor {
    fun isLikely(factors: SolForecastFactors): Boolean
}