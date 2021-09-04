package com.kylecorry.trailsensecore.meteorology.clouds

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import com.kylecorry.trailsensecore.geology.GeologyService
import com.kylecorry.trailsensecore.geology.Region

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