package com.kylecorry.sol.science.meteorology.forecast.sol

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.PressureTendency
import com.kylecorry.sol.science.meteorology.WeatherCondition
import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature

internal data class SolForecastFactors(
    val pressureTendency: PressureTendency,
    val pressureSystem: PressureSystem?,
    val doCloudsIndicateWeatherFront: Boolean,
    val weatherFrontFromCloudsOnly: WeatherFront?,
    val weatherFront: WeatherFront?,
    val temperatureRangeDaily: Range<Temperature>? = null,
    val cloudReadingCurrent: Reading<CloudGenus?>? = null
)
