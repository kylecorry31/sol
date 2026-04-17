package com.kylecorry.sol.science.meteorology.forecast.sol.predictors

import com.kylecorry.sol.science.meteorology.PressureCharacteristic
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.forecast.sol.SolForecastFactors

internal class HighPressureLaterPredictor : WeatherPredictor {
    override fun isLikely(factors: SolForecastFactors): Boolean {
        return (
            factors.weatherFront == WeatherFront.Cold &&
                factors.pressureSystem != PressureSystem.Low
            ) || (
            factors.pressureSystem == PressureSystem.High &&
                factors.pressureTendency.characteristic == PressureCharacteristic.Steady
            )
    }
}
