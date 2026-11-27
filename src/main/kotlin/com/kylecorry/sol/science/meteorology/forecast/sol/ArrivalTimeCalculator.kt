package com.kylecorry.sol.science.meteorology.forecast.sol

import com.kylecorry.sol.science.meteorology.WeatherCondition
import com.kylecorry.sol.science.meteorology.WeatherForecast
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.CloudPrecipitationCalculator
import com.kylecorry.sol.time.Time.middle
import com.kylecorry.sol.units.Reading
import java.time.Instant

internal object ArrivalTimeCalculator {
    fun getArrivalTime(
        time: Instant,
        forecast: WeatherForecast,
        clouds: List<Reading<CloudGenus?>>,
        doCloudsIndicatePrecipitation: Boolean
    ): Instant? {
        if (forecast.conditions.contains(WeatherCondition.Precipitation)) {
            if (!doCloudsIndicatePrecipitation) {
                return null
            }
            return CloudPrecipitationCalculator().getPrecipitationTime(clouds)?.middle()
        }

        return time.takeIf { forecast.conditions.isNotEmpty() }
    }
}
