package com.kylecorry.trailsensecore.domain.geo

import android.hardware.GeomagneticField
import com.kylecorry.trailsensecore.domain.geo.Coordinate

internal class DeclinationCalculator {
    fun calculate(location: Coordinate, altitude: Float): Float {
        val time: Long = System.currentTimeMillis()
        val geoField = GeomagneticField(location.latitude.toFloat(), location.longitude.toFloat(), altitude, time)
        return geoField.declination
    }
}