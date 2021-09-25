package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.science.meteorology.clouds.CloudService
import com.kylecorry.sol.science.meteorology.clouds.CloudType
import com.kylecorry.sol.science.meteorology.clouds.ICloudService
import com.kylecorry.sol.units.Reading

class CloudWeatherForecaster(
    private val cloudService: ICloudService = CloudService()
) : IWeatherForecaster<CloudType> {

    override fun forecast(readings: List<Reading<CloudType>>): WeatherForecast {
        val last = readings.lastOrNull() ?: return WeatherForecast(Weather.Unknown, 0f)
        val tendency = getTendency(readings)

        val gettingWorse = tendency > 0
        val gettingBetter = tendency < 0

        val currentPrecipitation = cloudService.getCloudPrecipitationPercentage(last.value)

        if (currentPrecipitation == 1f) {
            return WeatherForecast(Weather.Storm, 1f)
        }

        return if (gettingWorse && currentPrecipitation <= 0.5f) {
            return WeatherForecast(Weather.WorseningSlow, currentPrecipitation)
        } else if (gettingWorse) {
            WeatherForecast(Weather.WorseningFast, currentPrecipitation)
        } else if (gettingBetter && currentPrecipitation > 0.5f) {
            WeatherForecast(Weather.ImprovingSlow, currentPrecipitation)
        } else if (gettingBetter) {
            WeatherForecast(Weather.ImprovingFast, currentPrecipitation)
        } else {
            WeatherForecast(Weather.NoChange, currentPrecipitation)
        }
    }

    private fun getTendency(readings: List<Reading<CloudType>>): Float {
        val first = readings.firstOrNull() ?: return 0f
        val last = readings.lastOrNull() ?: return 0f

        return cloudService.getCloudPrecipitationPercentage(last.value) - cloudService.getCloudPrecipitationPercentage(
            first.value
        )
    }

}