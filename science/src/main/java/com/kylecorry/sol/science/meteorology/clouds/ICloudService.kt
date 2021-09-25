package com.kylecorry.sol.science.meteorology.clouds

import com.kylecorry.sol.units.Coordinate

interface ICloudService {
    fun getCloudPrecipitation(cloud: CloudType): CloudWeather
    fun getCloudPrecipitationPercentage(cloud: CloudType): Float
    fun getCloudHeightRange(height: CloudHeight, location: Coordinate): HeightRange
    fun getCloudsByShape(shape: CloudShape): List<CloudType>
    fun getCloudsByHeight(height: CloudHeight): List<CloudType>
    fun getCloudsByColor(color: CloudColor): List<CloudType>
}