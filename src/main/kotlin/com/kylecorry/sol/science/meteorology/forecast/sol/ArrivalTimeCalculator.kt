package com.kylecorry.sol.science.meteorology.forecast.sol

import com.kylecorry.sol.science.meteorology.PressureCharacteristic
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
        clouds: List<Reading<CloudGenus?>>
    ): Instant? {

        val hasSkyCondition =
            forecast.conditions.contains(WeatherCondition.Clear) || forecast.conditions.contains(
                WeatherCondition.Overcast
            )
        val isSteady = forecast.tendency?.characteristic == PressureCharacteristic.Steady
        val hasSingleCondition = forecast.conditions.size == 1
        val hasWindCondition = forecast.conditions.contains(WeatherCondition.Wind)

        if (hasSingleCondition && ((hasSkyCondition && isSteady) || hasWindCondition)) {
            return time
        }

        // Can't currently predict the time for other conditions
        if (!forecast.conditions.contains(WeatherCondition.Precipitation)) {
            return null
        }

        val cloudIndicatedArrivalRange = CloudPrecipitationCalculator().getPrecipitationTime(clouds)

        return cloudIndicatedArrivalRange?.middle()
    }
}