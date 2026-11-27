package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Temperature

/**
 * The temperatures and normal climate used to forecast the weather. Anything unknown is null.
 *
 * @param dailyTemperatureRange the expected temperature range for the day of the forecast
 * @param monthlyPrecipitation the normal total precipitation for the month of the forecast
 */
data class ForecastClimate(
    val dailyTemperatureRange: Range<Temperature>? = null,
    val monthlyPrecipitation: Distance? = null
)
