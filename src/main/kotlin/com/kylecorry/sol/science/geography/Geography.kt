package com.kylecorry.sol.science.geography

import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.real
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.optimization.LeastSquaresOptimizer
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
        val bearing = reference.coordinate.bearingTo(destination.coordinate)
        val distance = Distance.meters(reference.horizontalDistanceTo(destination))
        val elevationAngle = reference.inclinationTo(destination)
        return toENU(bearing, elevationAngle, distance)
    }

    /**
     * Converts a spherical coordinate to a cartesian coordinate in the East-North-Up (ENU) coordinate system.
     * @param bearing The azimuth (positive is clockwise from north)
     * @param elevation The elevation angle (positive is up)
     * @param distance The distance
     * @return The ENU coordinate
     */
    fun toENU(
        bearing: Angle,
        elevation: Angle,
        distance: Distance
    ): Vector3 {
        val elevationRad = elevation.radians().value
        val bearingRad = bearing.radians().value
        val distanceMeters = distance.meters().value

        val cosElevation = cos(elevationRad)
        val x = distanceMeters * cosElevation * sin(bearingRad) // East
        val y = distanceMeters * cosElevation * cos(bearingRad) // North
        val z = distanceMeters * sin(elevationRad) // Up
        return Vector3(x, y, z)
    }

    /**
     * Calculates the bearing from an ENU coordinate.
     * @param enu The ENU coordinate
     * @return The bearing (positive is clockwise from north)
     */
    fun getBearingFromENU(enu: Vector3): Angle {
        return Angle.radians(atan2(enu.x, enu.y).real(0f), true)
    }

    /**
     * Calculates the elevation from an ENU coordinate.
     * @param enu The ENU coordinate
     * @return The elevation in degrees (positive is up)
     */
    fun getElevationFromENU(enu: Vector3): Angle {
        return Angle.radians(asin(enu.z / enu.magnitude()).real(0f))
    }

    /**
     * Calculates the distance from an ENU coordinate.
     * @param enu The ENU coordinate
     * @return The distance in the units of the ENU coordinate
     */
    fun getDistanceFromENU(enu: Vector3): Distance {
        return Distance.meters(enu.magnitude())
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
        val x = (n + location.elevation.meters().value) * cos(lat) * cos(lon)
        val y = (n + location.elevation.meters().value) * cos(lat) * sin(lon)
        val z = (n * (1 - square(e)) + location.elevation.meters().value) * sin(lat)
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

    fun trilaterate(
        readings: List<Geofence>,
        isWeighted: Boolean = false,
        calculateBias: Boolean = false
    ): TrilaterationResult {
        if (readings.size < 2) {
            return TrilaterationResult(readings.firstOrNull()?.center?.let { listOf(it) } ?: listOf())
        }

        // There are 2 possible solutions for 2 readings
        if (readings.size == 2) {
            return TrilaterationResult(trilaterate2(readings))
        }

        val optimizer = LeastSquaresOptimizer()
        val distanceFn = { point: List<Float>, guess: List<Float> ->
            Coordinate(point[0].toDouble(), point[1].toDouble()).degreesBetween(
                Coordinate(
                    guess[0].toDouble(),
                    guess[1].toDouble()
                )
            )
        }

        val distanceFnWithCorrection = { point: List<Float>, guess: List<Float> ->
            distanceFn(point, guess) + if (calculateBias) guess[2] else 0f
        }

        val weightingFn = { index: Int, point: List<Float>, error: Float ->
            if (isWeighted) {
                1f / (error * error + 1)
            } else {
                1f
            }
        }

        val errors = readings.map { it.radius.convertTo(DistanceUnits.NauticalMiles).value / 60f }

        val result = optimizer.optimize(
            readings.map {
                listOf(
                    it.center.latitude.toFloat(),
                    it.center.longitude.toFloat()
                ) + if (calculateBias) listOf(0f) else emptyList()
            },
            errors,
            maxIterations = 200,
            dampingFactor = 1f,
            tolerance = 0.000001f,
            weightingFn = weightingFn,
            distanceFn = distanceFnWithCorrection,
            jacobianFn = { index, point, guess ->
                val distance = distanceFn(point, guess)
                point.mapIndexed { j, value ->
                    if (j < 2) {
                        deltaAngle(value, guess[j]) / distance * weightingFn(index, point, errors[index])
                    } else {
                        1f
                    }
                }
            }
        )

        return TrilaterationResult(
            listOf(
                Coordinate.constrained(
                    result[0].toDouble(),
                    Coordinate.toLongitude(result[1].toDouble())
                )
            ), if (calculateBias) result[2] else null
        )
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

        val radii = readings.map { (it.radius.convertTo(DistanceUnits.NauticalMiles).value / 60f).toRadians() }

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

        return listOf(Coordinate.constrained(latitude1, longitude1), Coordinate.constrained(latitude2, longitude2))
    }
}