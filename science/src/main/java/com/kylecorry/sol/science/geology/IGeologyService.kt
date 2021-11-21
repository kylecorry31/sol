package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.Vector2
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
    fun getInclination(gravity: Vector3): Float

    /**
     * Determine the avalanche risk of a slope
     * @param inclination The inclination angle (degrees)
     * @return The avalanche risk
     */
    fun getAvalancheRisk(inclination: Float): AvalancheRisk

    // Coordinates / navigation

    fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean

    fun getRegion(coordinate: Coordinate): Region

    fun getMapDistance(measurement: Distance, scaleFrom: Distance, scaleTo: Distance): Distance

    fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance

    fun toMercator(coordinate: Coordinate, bounds: CoordinateBounds, size: Pair<Float, Float>): Vector2

    fun fromMercator(point: Vector2, bounds: CoordinateBounds, size: Pair<Float, Float>): Coordinate

    fun getBounds(points: List<Coordinate>): CoordinateBounds

    fun triangulate(pointA: Coordinate, bearingA: Bearing, pointB: Coordinate, bearingB: Bearing): Coordinate?

    fun deadReckon(lastLocation: Coordinate, distanceTravelled: Float, bearingToLast: Bearing): Coordinate

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float,
        useTrueNorth: Boolean
    ): NavigationVector

    fun getCrossTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate): Distance

    fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate

    fun getPathDistance(points: List<Coordinate>): Distance

    fun getElevationGain(elevations: List<Distance>, threshold: Distance = Distance.meters(0f)): Distance

    fun getElevationLoss(elevations: List<Distance>, threshold: Distance = Distance.meters(0f)): Distance

}