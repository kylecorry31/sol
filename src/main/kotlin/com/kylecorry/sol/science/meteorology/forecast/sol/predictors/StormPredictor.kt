package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors
import kotlin.math.absoluteValue

internal class StormPredictor(private val pressureTendencyStormThreshold: Float) : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        if (factors.pressureTendency.characteristic.isRising) {
            return false
        }

        if (factors.weatherFrontFromCloudsOnly == WeatherFront.Cold) {
            return true
        }

        return factors.pressureTendency.characteristic.isFalling && factors.pressureTendency.amount.absoluteValue >= pressureTendencyStormThreshold.absoluteValue
    }
}
