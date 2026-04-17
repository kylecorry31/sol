package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.PressureCharacteristic
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class OvercastLaterPredictor : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        val hasCurrentCloudConditions =
            factors.cloudReadingCurrent != null &&
                (
                    factors.cloudReadingCurrent.value == null ||
                        factors.cloudReadingCurrent.value in ForecastHelper.overcastClouds
                    )
        return (
            factors.weatherFront == WeatherFront.Warm &&
                factors.pressureSystem != PressureSystem.High
            ) || (
            hasCurrentCloudConditions &&
                factors.pressureSystem == PressureSystem.Low &&
                factors.pressureTendency.characteristic == PressureCharacteristic.Steady
            )
    }
}
