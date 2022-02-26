package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance

interface IGeologyService {

    // Geomagnetic field
    fun getGeomagneticDeclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getGeomagneticInclination(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Float

    fun getGeomagneticField(coordinate: Coordinate, altitude: Float? = null, time: Long = System.currentTimeMillis()): Vector3

    fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing

    // Gravity
    fun getGravity(coordinate: Coordinate): Float

    /**
     * Determine the avalanche risk of a slope
     * @param inclination The inclination angle (degrees)
     * @return The avalanche risk
     */
    fun getAvalancheRisk(inclination: Float): AvalancheRisk
    /**
     * Determines the grade (percent)
     * @param inclination The inclination angle (degrees)
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(inclination: Float): Float

    /**
     * Estimates the height of an object
     * @param distance The distance to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated height of the object
     */
    fun getHeightFromInclination(distance: Distance, bottomInclination: Float, topInclination: Float): Distance

    /**
     * Estimates the distance to an object
     * @param height The height to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated distance to the object
     */
    fun getDistanceFromInclination(height: Distance, bottomInclination: Float, topInclination: Float): Distance

    /**
     * Calculates the inclination from a unit angle
     * @param angle The angle, where 0 is the horizon (front), 90 is the sky (above), 180 is the horizon (behind), and 270 is the ground (below)
     */
    fun getInclination(angle: Float): Float

    // Coordinates / navigation

    fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean

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
        useTrueNorth: Boolean,
        highAccuracy: Boolean = true
    ): NavigationVector

    fun getCrossTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate): Distance

    fun getAlongTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate): Distance

    fun getNearestPoint(point: Coordinate, start: Coordinate, end: Coordinate): Coordinate

    fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate

    fun getPathDistance(points: List<Coordinate>, highAccuracy: Boolean = true): Distance

    fun getElevationGain(elevations: List<Distance>): Distance

    fun getElevationLoss(elevations: List<Distance>): Distance

}