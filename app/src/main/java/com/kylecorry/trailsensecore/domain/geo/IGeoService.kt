package com.kylecorry.trailsensecore.domain.geo

import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.geo.cartography.CoordinateBounds
import com.kylecorry.trailsensecore.domain.geo.cartography.MapSite

interface IGeoService {

    fun getDeclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getInclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getGeomagneticField(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Vector3

    fun getRegion(coordinate: Coordinate): Region

    fun getMapDistance(measurement: Distance, scaleFrom: Distance, scaleTo: Distance): Distance

    fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance

    fun getMapUrl(coordinate: Coordinate, map: MapSite): String

    fun getBounds(points: List<Coordinate>): CoordinateBounds

}