package com.kylecorry.trailsensecore.domain.geo

import android.hardware.GeomagneticField
import com.kylecorry.trailsensecore.domain.units.Distance
import kotlin.math.absoluteValue

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

    override fun getRegion(coordinate: Coordinate): Region {
        return when {
            coordinate.latitude.absoluteValue >= 66.5 -> Region.Polar
            coordinate.latitude.absoluteValue >= 23.5 -> Region.Temperate
            else -> Region.Tropical
        }
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