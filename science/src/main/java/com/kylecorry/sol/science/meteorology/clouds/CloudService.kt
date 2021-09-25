package com.kylecorry.sol.science.meteorology.clouds

import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.science.geology.GeologyService
import com.kylecorry.sol.science.geology.Region

class CloudService : ICloudService {

    private val geoService = GeologyService()

    override fun getCloudPrecipitation(cloud: CloudType): CloudWeather {
        return when (cloud) {
            CloudType.Cirrus -> CloudWeather.Fair
            CloudType.Cirrocumulus -> CloudWeather.Fair
            CloudType.Cirrostratus -> CloudWeather.Fair
            CloudType.Altocumulus -> CloudWeather.PrecipitationPossible
            CloudType.Altostratus -> CloudWeather.PrecipitationPossible
            CloudType.Nimbostratus -> CloudWeather.PrecipitationLikely
            CloudType.Stratus -> CloudWeather.PrecipitationPossible
            CloudType.Stratocumulus -> CloudWeather.PrecipitationPossible
            CloudType.Cumulus -> CloudWeather.PrecipitationPossible
            CloudType.Cumulonimbus -> CloudWeather.StormLikely
        }
    }

    override fun getCloudPrecipitationPercentage(cloud: CloudType): Float {
        // TODO: Find a better source for these values
        return when (cloud) {
            CloudType.Cirrus -> 0f
            CloudType.Cirrocumulus -> 0f
            CloudType.Cirrostratus -> 0f
            CloudType.Altocumulus -> 0.1f
            CloudType.Altostratus -> 0.2f
            CloudType.Nimbostratus -> 1f
            CloudType.Stratus -> 0.75f
            CloudType.Stratocumulus -> 0.6f
            CloudType.Cumulus -> 0.5f
            CloudType.Cumulonimbus -> 1f
        }
    }

    override fun getCloudHeightRange(height: CloudHeight, location: Coordinate): HeightRange {
        if (height == CloudHeight.Low) {
            return HeightRange(
                Distance(0f, DistanceUnits.Kilometers),
                Distance(2f, DistanceUnits.Kilometers)
            )
        }

        val region = geoService.getRegion(location)
        val highStart = when (region) {
            Region.Polar -> 3f
            Region.Temperate -> 5f
            Region.Tropical -> 6f
        }

        val highEnd = when (region) {
            Region.Polar -> 8f
            Region.Temperate -> 14f
            Region.Tropical -> 18f
        }

        return when (height) {
            CloudHeight.Middle -> HeightRange(
                Distance(2f, DistanceUnits.Kilometers),
                Distance(highStart, DistanceUnits.Kilometers)
            )
            else -> HeightRange(
                Distance(highStart, DistanceUnits.Kilometers),
                Distance(highEnd, DistanceUnits.Kilometers)
            )
        }
    }

    override fun getCloudsByShape(shape: CloudShape): List<CloudType> {
        return CloudType.values().filter { it.shape.contains(shape) }
    }

    override fun getCloudsByHeight(height: CloudHeight): List<CloudType> {
        return CloudType.values().filter { it.height == height }
    }

    override fun getCloudsByColor(color: CloudColor): List<CloudType> {
        return CloudType.values().filter { it.colors.contains(color) }
    }

}