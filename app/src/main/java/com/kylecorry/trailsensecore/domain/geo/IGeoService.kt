package com.kylecorry.trailsensecore.domain.geo

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.trailsensecore.domain.units.Distance

interface IGeoService {

    fun getDeclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getAzimuth(gravity: FloatArray, magneticField: FloatArray): Bearing?

    fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing?

    fun getRegion(coordinate: Coordinate): Region

    fun getMapDistance(measurement: Distance, scaleFrom: Distance, scaleTo: Distance): Distance

    fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance

}