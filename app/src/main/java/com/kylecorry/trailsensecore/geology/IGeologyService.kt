package com.kylecorry.trailsensecore.geology

import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance

interface IGeologyService {

    // Geomagnetic field
    fun getMagneticDeclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getMagneticInclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getGeomagneticField(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Vector3

    fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing

    // Gravity
    fun getInclination(gravity: Vector3): Float

    /**
     * Determine the avalanche risk of a slope
     * @param inclination The inclination angle (degrees)
     * @return The avalanche risk
     */
    fun getAvalancheRisk(inclination: Float): AvalancheRisk

    // Coordinates / navigation
    fun getRegion(coordinate: Coordinate): Region

    fun getMapDistance(measurement: Distance, scaleFrom: Distance, scaleTo: Distance): Distance

    fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance

    fun getBounds(points: List<Coordinate>): CoordinateBounds

    fun triangulate(pointA: Coordinate, bearingA: Bearing, pointB: Coordinate, bearingB: Bearing): Coordinate?

    fun deadReckon(lastLocation: Coordinate, distanceTravelled: Float, bearingToLast: Bearing): Coordinate

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float,
        useTrueNorth: Boolean
    ): NavigationVector

    fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate

    fun getPathDistance(points: List<Coordinate>): Distance

}