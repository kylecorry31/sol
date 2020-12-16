package com.kylecorry.trailsensecore.domain.geo

import android.hardware.GeomagneticField

class GeoService : IGeoService {

    override fun getDeclination(coordinate: Coordinate, altitude: Float?, time: Long): Float {
        val geoField = GeomagneticField(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return geoField.declination
    }
}