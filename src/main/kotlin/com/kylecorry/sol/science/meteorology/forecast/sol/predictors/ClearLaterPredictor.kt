package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.PressureCharacteristic
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class ClearLaterPredictor : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        val hasCurrentCloudConditions =
            factors.cloudReadingCurrent != null &&
                (
                    factors.cloudReadingCurrent.value == null ||
                        factors.cloudReadingCurrent.value in ForecastHelper.overcastClouds
                    )
        return (
            factors.weatherFront == WeatherFront.Cold &&
                factors.pressureSystem != PressureSystem.Low
            ) || (
            hasCurrentCloudConditions &&
                factors.pressureSystem == PressureSystem.High &&
                factors.pressureTendency.characteristic == PressureCharacteristic.Steady
            )
    }
}
