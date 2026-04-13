package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class WindPredictor : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        return factors.pressureTendency.characteristic.isRapid || (factors.weatherFrontFromCloudsOnly == WeatherFront.Cold && !factors.pressureTendency.characteristic.isRising)
    }
}