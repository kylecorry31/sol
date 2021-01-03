package com.kylecorry.trailsensecore.domain.geo

import android.hardware.GeomagneticField
import com.kylecorry.trailsensecore.domain.units.Distance

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

    override fun getMapDistance(
        measurement: Distance,
        scaleFrom: Distance,
        scaleTo: Distance
    ): Distance {
        val scaledMeasurement = measurement.convertTo(scaleFrom.units)
        return Distance(scaleTo.distance * scaledMeasurement.distance / scaleFrom.distance, scaleTo.units)
    }

    override fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance {
        return Distance(ratioTo * measurement.distance / ratioFrom, measurement.units)
    }
}