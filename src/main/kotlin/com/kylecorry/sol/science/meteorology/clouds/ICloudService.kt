package com.kylecorry.sol.science.meteorology.clouds

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.Precipitation
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity

interface ICloudService {
    /**
     * Get the likely precipitation types for the given cloud
     * @param cloud the type of cloud
     * @return the types of precipitation the cloud can produce
     */
    fun getPrecipitation(cloud: CloudGenus): List<Precipitation>

    /**
     * Get the likelihood that the cloud will precipitate
     * @param cloud the type of cloud
     * @return the chance that it will precipitate [0, 1]
     */
    fun getPrecipitationChance(cloud: CloudGenus): Float

    /**
     * Get the height range of the cloud layer
     * @param level the cloud layer
     * @param location the location
     * @return the height range of the cloud layer
     */
    fun getHeightRange(level: CloudLevel, location: Coordinate): Range<Quantity<Distance>>

    /**
     * Get the cloud cover label
     * @param percent the percent cloud cover [0, 1]
     * @return the cloud cover classification
     */
    fun getCloudCover(percent: Float): CloudCover
}