package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class PrecipitationPredictor: WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        return (factors.weatherFront == WeatherFront.Warm || factors.doCloudsIndicateWeatherFront) && !factors.pressureTendency.characteristic.isRising
    }
}