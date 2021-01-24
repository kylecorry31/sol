package com.kylecorry.trailsensecore.domain.weather.clouds

import com.kylecorry.trailsensecore.domain.geo.Coordinate

interface ICloudService {
    fun getCloudPrecipitation(cloud: CloudType): CloudWeather
    fun getCloudHeightRange(height: CloudHeight, location: Coordinate): HeightRange
}