package com.kylecorry.sol.science.geography

import com.kylecorry.sol.math.SolMath.real
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.sumOfFloat
import com.kylecorry.sol.science.geography.projections.AzimuthalEquidistantProjection
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.science.geology.ReferenceEllipsoid
import com.kylecorry.sol.units.*
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

    fun trilaterate(readings: List<Geofence>): List<Coordinate> {
        if (readings.size < 2) {
            return readings.firstOrNull()?.center?.let { listOf(it) } ?: listOf()
        }

        // Step 1: Calculate all intersections
        val intersections = mutableListOf<Coordinate>()
        for (i in readings.indices) {
            for (j in i + 1 until readings.size) {
                intersections.addAll(trilaterate2(listOf(readings[i], readings[j])))
            }
        }
        // Step 2: Get the boundary of the intersection triangle (the points that are closest together)
        val boundary = intersections.mapIndexed { index, coordinate ->
            // Proximity to the other 2 points of the triangle
            val distance = intersections
                .filterIndexed { i, _ -> i != index }
                .map { it.distanceTo(coordinate) }
                .sorted()
                .take(2)
                .sumOfFloat { it }
            Pair(coordinate, distance)
        }
            .sortedBy { it.second }
            .take(3)
            .map { it.first }

        if (boundary.size <= 2) {
            return boundary
        }
        // Step 3: Calculate the center
        val projection = AzimuthalEquidistantProjection(boundary.first())
        val projected = boundary.map { projection.toPixels(it) }
        // Calculate center of mass
        var cx = 0f
        var cy = 0f
        var a = 0f

        for (i in 0 until projected.lastIndex) {
            cx += (projected[i].x + projected[i + 1].x) * (projected[i].x * projected[i + 1].y - projected[i + 1].x * projected[i].y)
            cy += (projected[i].y + projected[i + 1].y) * (projected[i].x * projected[i + 1].y - projected[i + 1].x * projected[i].y)
            a += projected[i].x * projected[i + 1].y - projected[i + 1].x * projected[i].y
        }

        cx /= 3 * a
        cy /= 3 * a

        val centerCoordinate = projection.toCoordinate(Vector2(cx, cy))

        return listOf(centerCoordinate)
    }

    private fun trilaterate2(readings: List<Geofence>): List<Coordinate> {
        val scale = 1000.0
        val cartesianPoints = readings.map {
            listOf(
                cos(it.center.longitude.toRadians()) * cos(it.center.latitude.toRadians()) * scale,
                sin(it.center.longitude.toRadians()) * cos(it.center.latitude.toRadians()) * scale,
                sin(it.center.latitude.toRadians()) * scale
            )
        }

        val radii = readings.map { (it.radius.convertTo(DistanceUnits.NauticalMiles).distance / 60f).toRadians() }

        val x1 = Vector3Precise.from(cartesianPoints[0].toDoubleArray())
        val x2 = Vector3Precise.from(cartesianPoints[1].toDoubleArray())
        val q = x1.dot(x2) / square(scale)
        val a = (cos(radii[0]) - cos(radii[1]) * q) / (1 - q * q)
        val b = (cos(radii[1]) - cos(radii[0]) * q) / (1 - q * q)
        val n = x1.cross(x2)

        val x0 = x1.times(a) + x2.times(b)

        val t = sqrt((square(scale) - x0.dot(x0)) / n.dot(n))

        val p1 = (x0 + n.times(t)).times(1 / scale)
        val p2 = (x0 - n.times(t)).times(1 / scale)

        val latitude1 = (atan2(p1.z, sqrt(square(p1.x) + square(p1.y)))).toDegrees()
        val longitude1 = (atan2(p1.y, p1.x)).toDegrees()

        val latitude2 = (atan2(p2.z, sqrt(square(p2.x) + square(p2.y)))).toDegrees()
        val longitude2 = (atan2(p2.y, p2.x)).toDegrees()

        return listOf(
            Coordinate(latitude1, longitude1),
            Coordinate(latitude2, longitude2)
        ).filter { !it.latitude.isNaN() && !it.longitude.isNaN() }
    }
}