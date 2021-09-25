package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.science.meteorology.clouds.CloudService
import com.kylecorry.sol.science.meteorology.clouds.CloudType
import com.kylecorry.sol.science.meteorology.clouds.ICloudService
import com.kylecorry.sol.units.Reading

class CloudWeatherForecaster(
    private val cloudService: ICloudService = CloudService()
) : IWeatherForecaster<CloudType> {

    override fun forecast(readings: List<Reading<CloudType>>): Float {
        // TODO: This only determines bad weather - factor in the change in clouds to determine good weather
        val last = readings.lastOrNull() ?: return 0f

        val currentPrecipitation = cloudService.getCloudPrecipitationPercentage(last.value)

        if (currentPrecipitation == 1f) {
            return -1f
        }

        return -currentPrecipitation
    }

}