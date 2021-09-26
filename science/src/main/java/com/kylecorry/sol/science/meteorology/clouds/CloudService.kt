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
        // Using average values from table 9: https://www.ideals.illinois.edu/bitstream/handle/2142/101973/ISWSRI-33.pdf?sequence=1&isAllowed=y
        return when (cloud) {
            CloudType.Cirrus -> 0.06f
            CloudType.Cirrocumulus -> 0.06f
            CloudType.Cirrostratus -> 0.06f
            CloudType.Altocumulus -> 0.02f
            CloudType.Altostratus -> 0.23f
            CloudType.Nimbostratus -> 1f
            CloudType.Stratus -> 0.21f
            CloudType.Stratocumulus -> 0.17f
            CloudType.Cumulus -> 0.24f
            CloudType.Cumulonimbus -> 0.41f
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

    override fun getCloudCover(percent: Float): CloudCover {
        return when {
            percent < 0.01f -> {
                CloudCover.NoClouds
            }
            percent < 0.1f -> {
                CloudCover.Few
            }
            percent < 0.25f -> {
                CloudCover.Isolated
            }
            percent < 0.5f -> {
                CloudCover.Scattered
            }
            percent < 0.9f -> {
                CloudCover.Broken
            }
            else -> {
                CloudCover.Overcast
            }
        }
    }

}