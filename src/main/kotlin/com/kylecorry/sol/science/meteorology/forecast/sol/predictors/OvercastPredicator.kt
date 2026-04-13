package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.PressureCharacteristic
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class OvercastPredicator : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        // Determine if an overcast cloud reading is present
        if (factors.cloudReadingCurrent != null && factors.cloudReadingCurrent.value in ForecastHelper.overcastClouds) {
            return true
        }

        return factors.cloudReadingCurrent == null && factors.pressureSystem == PressureSystem.Low && (factors.pressureTendency.characteristic == PressureCharacteristic.Steady || factors.pressureTendency.characteristic.isFalling)
    }
}