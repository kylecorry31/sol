package com.kylecorry.sol.science.meteorology.clouds

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.science.geology.GeologyService
import com.kylecorry.sol.science.geology.Region
import com.kylecorry.sol.science.meteorology.Precipitation

class CloudService : ICloudService {

    private val geoService = GeologyService()

    override fun getPrecipitation(cloud: CloudGenus): List<Precipitation> {
        return when (cloud) {
            CloudGenus.Altostratus -> listOf(
                Precipitation.Rain,
                Precipitation.Snow,
                Precipitation.IcePellets
            )
            CloudGenus.Nimbostratus -> listOf(
                Precipitation.Rain,
                Precipitation.Snow,
                Precipitation.IcePellets
            )
            CloudGenus.Stratus -> listOf(
                Precipitation.Drizzle,
                Precipitation.Snow,
                Precipitation.SnowGrains
            )
            CloudGenus.Stratocumulus -> listOf(
                Precipitation.Rain,
                Precipitation.Drizzle,
                Precipitation.Snow,
                Precipitation.SnowPellets
            )
            CloudGenus.Cumulus -> listOf(
                Precipitation.Rain,
                Precipitation.Snow,
                Precipitation.SnowPellets
            )
            CloudGenus.Cumulonimbus -> listOf(
                Precipitation.Rain,
                Precipitation.Snow,
                Precipitation.SnowPellets,
                Precipitation.Hail,
                Precipitation.SmallHail,
                Precipitation.Lightning
            )
            else -> emptyList()
        }
    }

    override fun getPrecipitationChance(cloud: CloudGenus): Float {
        // Using average values from table 9: https://www.ideals.illinois.edu/bitstream/handle/2142/101973/ISWSRI-33.pdf?sequence=1&isAllowed=y
        return when (cloud) {
            CloudGenus.Cirrus -> 0f
            CloudGenus.Cirrocumulus -> 0f
            CloudGenus.Cirrostratus -> 0f
            CloudGenus.Altocumulus -> 0f
            CloudGenus.Altostratus -> 0.23f
            CloudGenus.Nimbostratus -> 1f
            CloudGenus.Stratus -> 0.21f
            CloudGenus.Stratocumulus -> 0.17f
            CloudGenus.Cumulus -> 0.24f
            CloudGenus.Cumulonimbus -> 1f
        }
    }

    override fun getHeightRange(level: CloudLevel, location: Coordinate): Range<Distance> {
        if (level == CloudLevel.Low) {
            return Range(
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

        return when (level) {
            CloudLevel.Mid -> Range(
                Distance(2f, DistanceUnits.Kilometers),
                Distance(highStart, DistanceUnits.Kilometers)
            )
            else -> Range(
                Distance(highStart, DistanceUnits.Kilometers),
                Distance(highEnd, DistanceUnits.Kilometers)
            )
        }
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