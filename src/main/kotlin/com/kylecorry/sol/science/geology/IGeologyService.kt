package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure

interface IGeologyService {

    // Geomagnetic field
    fun getGeomagneticDeclination(
        coordinate: Coordinate,
        altitude: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Float

    fun getGeomagneticInclination(
        coordinate: Coordinate,
        altitude: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Float

    fun getGeomagneticField(
        coordinate: Coordinate,
        altitude: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Vector3

    fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing

    // Altitude
    fun getAltitude(pressure: Pressure, seaLevelPressure: Pressure): Distance

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
     * Determines the grade (percent)
     * @param horizontal The horizontal distance
     * @param vertical The vertical distance
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(horizontal: Distance, vertical: Distance): Float

    /**
     * Determines the grade (percent)
     * @param start The starting coordinate
     * @param startElevation The starting elevation
     * @param end The ending coordinate
     * @param endElevation The ending elevation
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(start: Coordinate, startElevation: Distance, end: Coordinate, endElevation: Distance): Float


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

    fun getInclinationFromSlopeGrade(grade: Float): Float
    fun getInclination(distance: Distance, elevationChange: Distance): Float

    // Coordinates / navigation

    fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean

    fun getRegion(coordinate: Coordinate): Region

    fun getMapDistance(measurement: Distance, scaleFrom: Distance, scaleTo: Distance): Distance

    fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance

    fun getBounds(points: List<Coordinate>): CoordinateBounds

    /**
     * Triangulate a coordinate using two known coordinates and the bearings from the unknown coordinate to the known coordinates.
     * Use this if you want to find your location by taking readings to two known locations.
     * @param referenceA The first known coordinate
     * @param selfToReferenceBearingA The bearing from the unknown coordinate to the first known coordinate (True North)
     * @param referenceB The second known coordinate
     * @param selfToReferenceBearingB The bearing from the unknown coordinate to the second known coordinate (True North)
     * @return The triangulated coordinate, if possible
     */
    fun triangulateSelf(
        referenceA: Coordinate,
        selfToReferenceBearingA: Bearing,
        referenceB: Coordinate,
        selfToReferenceBearingB: Bearing
    ): Coordinate?

    /**
     * Triangulate a coordinate using two known coordinates and the bearings from the known coordinates to the unknown coordinate.
     * Use this if you want to find the location of a destination by taking readings at two known locations.
     * @param referenceA The first known coordinate
     * @param referenceAToDestinationBearing The bearing from the first known coordinate to the unknown coordinate (True North)
     * @param referenceB The second known coordinate
     * @param referenceBToDestinationBearing The bearing from the second known coordinate to the unknown coordinate (True North)
     * @return The triangulated coordinate, if possible
     */
    fun triangulateDestination(
        referenceA: Coordinate,
        referenceAToDestinationBearing: Bearing,
        referenceB: Coordinate,
        referenceBToDestinationBearing: Bearing
    ): Coordinate?

    fun deadReckon(lastLocation: Coordinate, distanceTravelled: Float, bearingToLast: Bearing): Coordinate

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float = 0f,
        useTrueNorth: Boolean = true,
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