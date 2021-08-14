package com.kylecorry.trailsensecore.domain.geo

import android.hardware.GeomagneticField
import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.geo.cartography.MapSite
import com.kylecorry.trailsensecore.domain.geo.cartography.MapSiteService
import kotlin.math.absoluteValue

class GeoService : IGeoService {

    private val mapUrlGenerator = MapSiteService()

    override fun getDeclination(coordinate: Coordinate, altitude: Float?, time: Long): Float {
        val geoField = GeomagneticField(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return geoField.declination
    }

    override fun getInclination(coordinate: Coordinate, altitude: Float?, time: Long): Float {
        val geoField = GeomagneticField(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return geoField.inclination
    }

    override fun getGeomagneticField(
        coordinate: Coordinate,
        altitude: Float?,
        time: Long
    ): Vector3 {
        val geoField = GeomagneticField(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return Vector3(geoField.x * 0.001f, geoField.y * 0.001f, geoField.z * 0.001f)
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
        return Distance(
            scaleTo.distance * scaledMeasurement.distance / scaleFrom.distance,
            scaleTo.units
        )
    }

    override fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance {
        return Distance(ratioTo * measurement.distance / ratioFrom, measurement.units)
    }

    override fun getMapUrl(coordinate: Coordinate, map: MapSite): String {
        return mapUrlGenerator.getUrl(coordinate, map)
    }
}