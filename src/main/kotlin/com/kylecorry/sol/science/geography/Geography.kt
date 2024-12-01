package com.kylecorry.sol.science.geography

import com.kylecorry.sol.math.SolMath.real
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.optimization.LeastSquaresOptimizer
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.science.geology.ReferenceEllipsoid
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Location
import kotlin.math.*

object Geography {

    /**
     * Converts a geographic coordinate to an ENU coordinate.
     * @param reference The reference location
     * @param destination The destination location
     * @return The ENU coordinate
     */
    fun toENU(
        reference: Location,
        destination: Location
    ): Vector3 {
        val bearing = reference.coordinate.bearingTo(destination.coordinate).value
        val distance = reference.horizontalDistanceTo(destination)
        val elevationAngle = reference.inclinationTo(destination)
        return toENU(bearing, elevationAngle, distance)
    }

    /**
     * Converts a spherical coordinate to a cartesian coordinate in the East-North-Up (ENU) coordinate system.
     * @param bearing The azimuth in degrees (positive is clockwise from north)
     * @param elevation The elevation in degrees (positive is up)
     * @param distance The distance in meters
     * @return The ENU coordinate
     */
    fun toENU(
        bearing: Float,
        elevation: Float,
        distance: Float
    ): Vector3 {
        val elevationRad = elevation.toRadians()
        val bearingRad = bearing.toRadians()

        val cosElevation = cos(elevationRad)
        val x = distance * cosElevation * sin(bearingRad) // East
        val y = distance * cosElevation * cos(bearingRad) // North
        val z = distance * sin(elevationRad) // Up
        return Vector3(x, y, z)
    }

    /**
     * Calculates the bearing from an ENU coordinate.
     * @param enu The ENU coordinate
     * @return The bearing (positive is clockwise from north)
     */
    fun getBearingFromENU(enu: Vector3): Bearing {
        return Bearing(atan2(enu.x, enu.y).toDegrees().real(0f))
    }

    /**
     * Calculates the elevation from an ENU coordinate.
     * @param enu The ENU coordinate
     * @return The elevation in degrees (positive is up)
     */
    fun getElevationFromENU(enu: Vector3): Float {
        return asin(enu.z / enu.magnitude()).toDegrees().real(0f)
    }

    /**
     * Calculates the distance from an ENU coordinate.
     * @param enu The ENU coordinate
     * @return The distance in the units of the ENU coordinate
     */
    fun getDistanceFromENU(enu: Vector3): Float {
        return enu.magnitude()
    }

    /**
     * Calculates the ECEF coordinate from a geographic coordinate.
     * @param location The geographic coordinate (elevation needs to be above WGS84 ellipsoid)
     * @return The ECEF coordinate in meters
     */
    fun getECEF(location: Location): Vector3 {
        val lat = location.coordinate.latitude.toRadians()
        val lon = location.coordinate.longitude.toRadians()
        val a = ReferenceEllipsoid.wgs84.a
        val b = ReferenceEllipsoid.wgs84.b
        val e = sqrt(1 - square(b) / square(a))
        val n = a / sqrt(1 - square(e) * square(sin(lat)))
        val x = (n + location.elevation.meters().distance) * cos(lat) * cos(lon)
        val y = (n + location.elevation.meters().distance) * cos(lat) * sin(lon)
        val z = (n * (1 - square(e)) + location.elevation.meters().distance) * sin(lat)
        return Vector3(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * Calculates the geographic coordinate from an ECEF coordinate.
     * @param ecef The ECEF coordinate in meters
     * @return The geographic coordinate
     */
    fun getLocationFromECEF(ecef: Vector3): Location {
        val x = ecef.x.toDouble()
        val y = ecef.y.toDouble()
        val z = ecef.z.toDouble()
        val a = ReferenceEllipsoid.wgs84.a
        val b = ReferenceEllipsoid.wgs84.b
        val e = sqrt(1 - square(b) / square(a))
        val p = sqrt(square(x) + square(y))
        val theta = atan2(z * a, p * b)
        val lon = atan2(y, x)
        val lat = atan2(
            z + square(e) * b * sin(theta).pow(3),
            p - square(e) * a * cos(theta).pow(3)
        )
        val n = a / sqrt(1 - square(e) * square(sin(lat)))
        val alt = p / cos(lat) - n
        return Location(
            Coordinate(lat.toDegrees(), lon.toDegrees()),
            Distance.meters(alt.toFloat())
        )
    }


    fun trilaterate(readings: List<Geofence>): Geofence {
        require(readings.size >= 2) { "At least two readings are required for trilateration." }

        val cartesianPoints = readings.map {
            getECEF(Location(it.center, Distance.meters(0f))).toFloatArray().toList()
        }

        val optimizer = LeastSquaresOptimizer()
        val result = optimizer.optimize(cartesianPoints, readings.map { it.radius.meters().distance })
        val resultLocation = getLocationFromECEF(Vector3(result[0], result[1], result[2])).coordinate
        val averageError = readings.map { abs(it.center.distanceTo(resultLocation)) }.average().toFloat()
        return Geofence(resultLocation, Distance.meters(averageError))
    }


}