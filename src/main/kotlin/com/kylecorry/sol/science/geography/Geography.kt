package com.kylecorry.sol.science.geography

import com.kylecorry.sol.math.SolMath.real
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.deltaAngle
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.math.arithmetic.Arithmetic.clamp
import com.kylecorry.sol.math.arithmetic.Arithmetic.power
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.math.arithmetic.Arithmetic.wrap
import com.kylecorry.sol.math.optimization.LeastSquaresOptimizer
import com.kylecorry.sol.science.geology.*
import com.kylecorry.sol.units.*
import kotlin.math.*

object Geography {

    private const val EARTH_AVERAGE_RADIUS = 6371.2e3

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
        return Bearing.from(atan2(enu.x, enu.y).toDegrees().real(0f))
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

    /**
     * Computes the distance and bearing between two locations using the Vincenty formula.
     *
     * Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf using the "Inverse Formula" (section 4).
     * Originally from the Android Open Source Project (Apache 2.0), modified by Kyle Corry in 2021.
     *
     * @return a 3 element float array of the following [distance (m), initial bearing (true north), final bearing (true north)]
     */
    fun vincenty(
        location1: Coordinate,
        location2: Coordinate
    ): FloatArray {
        val lat1 = location1.latitude.toRadians()
        val lon1 = location1.longitude.toRadians()
        val lat2 = location2.latitude.toRadians()
        val lon2 = location2.longitude.toRadians()
        val MAXITERS = 20
        val a = 6378137.0 // WGS84 major axis
        val b = 6356752.3142 // WGS84 semi-major axis
        val f = (a - b) / a
        val aSqMinusBSqOverBSq = (a * a - b * b) / (b * b)
        val L = lon2 - lon1
        var A = 0.0
        val U1 = atan((1.0 - f) * tan(lat1))
        val U2 = atan((1.0 - f) * tan(lat2))
        val cosU1 = cos(U1)
        val cosU2 = cos(U2)
        val sinU1 = sin(U1)
        val sinU2 = sin(U2)
        val cosU1cosU2 = cosU1 * cosU2
        val sinU1sinU2 = sinU1 * sinU2
        var sigma = 0.0
        var deltaSigma = 0.0
        var cosSqAlpha: Double
        var cos2SM: Double
        var cosSigma: Double
        var sinSigma: Double
        var cosLambda = 0.0
        var sinLambda = 0.0
        var lambda = L // initial guess
        for (iter in 0 until MAXITERS) {
            val lambdaOrig = lambda
            cosLambda = cos(lambda)
            sinLambda = sin(lambda)
            val t1 = cosU2 * sinLambda
            val t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            val sinSqSigma = t1 * t1 + t2 * t2 // (14)
            sinSigma = sqrt(sinSqSigma)
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda // (15)
            sigma = atan2(sinSigma, cosSigma) // (16)
            val sinAlpha = if (sinSigma == 0.0) 0.0 else cosU1cosU2 * sinLambda / sinSigma // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            cos2SM =
                if (cosSqAlpha == 0.0) 0.0 else cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha // (18)
            val uSquared = cosSqAlpha * aSqMinusBSqOverBSq // defn
            A = 1 + uSquared / 16384.0 *  // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)))
            val B = uSquared / 1024.0 *  // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)))
            val C = f / 16.0 *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)) // (10)
            val cos2SMSq = cos2SM * cos2SM
            deltaSigma = (B * sinSigma *  // (6)
                    (cos2SM + B / 4.0 *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    B / 6.0 * cos2SM *
                                    (-3.0 + 4.0 * sinSigma * sinSigma) *
                                    (-3.0 + 4.0 * cos2SMSq))))
            lambda = L +
                    ((1.0 - C) * f * sinAlpha *
                            (sigma + (C * sinSigma *
                                    (cos2SM + (C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM)))))) // (11)
            val delta = (lambda - lambdaOrig) / lambda
            if (abs(delta) < 1.0e-12) {
                break
            }
        }
        val results = floatArrayOf(0f, 0f, 0f)
        val distance = (b * A * (sigma - deltaSigma)).toFloat()
        results[0] = distance
        val initialBearing = atan2(
            cosU2 * sinLambda,
            cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
        ).toFloat().toDegrees()
        results[1] = initialBearing
        val finalBearing = atan2(
            cosU1 * sinLambda,
            -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda
        ).toFloat().toDegrees()
        results[2] = finalBearing
        return results
    }

    /**
     * Computes the distance and bearing between two locations using the Haversine formula.
     * Adapted from https://www.movable-type.co.uk/scripts/latlong.html
     *
     * @return a 3 element float array of the following [distance (m), initial bearing (true north), final bearing (true north)]
     */
    fun haversine(
        location1: Coordinate,
        location2: Coordinate,
        radius: Double = 6371.2e3
    ): FloatArray {
        val distance = getGreatCircleDistance(location1, location2, radius)
        val initial = getInitialGreatCircleBearing(location1, location2)
        val final = Bearing.getBearing(initial + 180)
        return floatArrayOf(distance, initial, final)
    }

    private fun getInitialGreatCircleBearing(from: Coordinate, to: Coordinate): Float {
        val deltaLongitude = (to.longitude - from.longitude).toRadians()

        val x =
            cosDegrees(from.latitude) * sinDegrees(to.latitude) - sinDegrees(from.latitude) * cosDegrees(
                to.latitude
            ) * cos(deltaLongitude)
        val y = sin(deltaLongitude) * cosDegrees(to.latitude)
        val theta = atan2(y, x)
        return Bearing.getBearing(theta.toDegrees().toFloat())
    }

    private fun getGreatCircleDistance(from: Coordinate, to: Coordinate, radius: Double): Float {
        val deltaLatitude = to.latitude.toRadians() - from.latitude.toRadians()
        val deltaLongitude = to.longitude.toRadians() - from.longitude.toRadians()

        val a = power(
            sin(deltaLatitude / 2),
            2
        ) + cosDegrees(from.latitude) * cosDegrees(to.latitude) * power(sin(deltaLongitude / 2), 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (radius * c).toFloat()
    }

    fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean {
        return area.contains(coordinate)
    }

    fun getRegion(coordinate: Coordinate): Region {
        return when {
            coordinate.latitude.absoluteValue >= 66.5 -> Region.Polar
            coordinate.latitude.absoluteValue >= 23.5 -> Region.Temperate
            else -> Region.Tropical
        }
    }

    fun getMapDistance(
        measurement: Distance,
        scaleFrom: Distance,
        scaleTo: Distance
    ): Distance {
        val scaledMeasurement = measurement.convertTo(scaleFrom.units)
        return Distance.from(
            scaleTo.value * scaledMeasurement.value / scaleFrom.value,
            scaleTo.units
        )
    }

    fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance {
        return Distance.from(ratioTo * measurement.value / ratioFrom, measurement.units)
    }

    fun getBounds(points: List<Coordinate>): CoordinateBounds {
        return CoordinateBounds.from(points)
    }

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
    ): Coordinate? {
        val deltaLat = referenceA.latitude - referenceB.latitude
        val deltaLng = referenceA.longitude - referenceB.longitude
        val angularDist = 2 * asin(
            sqrt(
                sinDegrees(deltaLat / 2) * sinDegrees(deltaLat / 2) + cosDegrees(referenceA.latitude) * cosDegrees(
                    referenceB.latitude
                ) * sinDegrees(deltaLng / 2) * sinDegrees(deltaLng / 2)
            )
        )

        val initialBearing = acos(
            (sinDegrees(referenceB.latitude) - sinDegrees(referenceA.latitude) * cos(angularDist)) / (sin(
                angularDist
            ) * cosDegrees(referenceA.latitude))
        )
        val finalBearing = acos(
            (sinDegrees(referenceA.latitude) - sinDegrees(referenceB.latitude) * cos(angularDist)) / (sin(
                angularDist
            ) * cosDegrees(referenceB.latitude))
        )

        val a1: Double
        val a2: Double
        if (sinDegrees(referenceB.longitude - referenceA.longitude) > 0) {
            a1 = selfToReferenceBearingA.inverse().value.toDouble().toRadians() - initialBearing
            a2 = 2 * Math.PI - finalBearing - selfToReferenceBearingB.inverse().value.toDouble().toRadians()
        } else {
            a1 = selfToReferenceBearingA.inverse().value.toDouble().toRadians() - (2 * Math.PI - initialBearing)
            a2 = finalBearing - selfToReferenceBearingB.inverse().value.toDouble().toRadians()
        }

        if (sin(a1) == 0.0 && sin(a2) == 0.0) {
            return null
        }

        if (sin(a1) * sin(a2) < 0.0) {
            return null
        }

        val a3 = acos(-cos(a1) * cos(a2) + sin(a1) * sin(a2) * cos(angularDist))
        val angularDist13 = atan2(sin(angularDist) * sin(a1) * sin(a2), cos(a2) + cos(a1) * cos(a3))
        val p3Lat = asin(
            sinDegrees(referenceA.latitude) * cos(angularDist13) + cosDegrees(referenceA.latitude) * sin(
                angularDist13
            ) * cosDegrees(selfToReferenceBearingA.inverse().value.toDouble())
        )
        val deltaP3Long = atan2(
            sinDegrees(selfToReferenceBearingA.inverse().value.toDouble()) * sin(angularDist13) * cosDegrees(referenceA.latitude),
            cos(angularDist13) - sinDegrees(referenceA.latitude) * sin(p3Lat)
        )
        val p3Lng = referenceA.longitude.toRadians() + deltaP3Long

        val normalizedLat = clamp(p3Lat.toDegrees(), -90.0, 90.0)
        val normalizedLng = wrap(p3Lng.toDegrees(), -180.0, 180.0)

        return Coordinate(normalizedLat, normalizedLng)
    }

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
    ): Coordinate? {
        return triangulateSelf(
            referenceA,
            referenceAToDestinationBearing.inverse(),
            referenceB,
            referenceBToDestinationBearing.inverse()
        )
    }

    fun deadReckon(
        lastLocation: Coordinate,
        distanceTravelled: Float,
        bearingToLast: Bearing
    ): Coordinate {
        return lastLocation.plus(distanceTravelled.toDouble(), bearingToLast.inverse())
    }

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float = 0f,
        useTrueNorth: Boolean = true,
        highAccuracy: Boolean = true
    ): NavigationVector {
        val results = if (highAccuracy) {
            vincenty(from, to)
        } else {
            haversine(from, to, EARTH_AVERAGE_RADIUS)
        }

        val declinationAdjustment = if (useTrueNorth) {
            0f
        } else {
            -declination
        }

        return NavigationVector(
            Bearing.from(results[1]).withDeclination(declinationAdjustment),
            results[0]
        )
    }

    fun getCrossTrackDistance(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Distance {
        // Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        if (point == start || point == end) {
            return Distance.meters(0f)
        }

        val startToPoint = haversine(start, point, EARTH_AVERAGE_RADIUS)
        val startToEnd = haversine(start, end, EARTH_AVERAGE_RADIUS)

        val distanceFromStart = startToPoint[0] / EARTH_AVERAGE_RADIUS
        val bearingFromStart = startToPoint[1].toRadians()
        val bearingLine = startToEnd[1].toRadians()

        val crossTrackDistanceRadians =
            asin(sin(distanceFromStart) * sin(bearingFromStart - bearingLine))
        val crossTrackDistance = crossTrackDistanceRadians * EARTH_AVERAGE_RADIUS

        return Distance.meters(crossTrackDistance.toFloat())
    }

    fun getAlongTrackDistance(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Distance {

        if (point == start) {
            return Distance.meters(0f)
        }

        if (point == end) {
            return Distance.meters(point.distanceTo(end))
        }

        val startToPoint = haversine(start, point, EARTH_AVERAGE_RADIUS)
        val startToEnd = haversine(start, end, EARTH_AVERAGE_RADIUS)

        val distanceFromStart = startToPoint[0] / EARTH_AVERAGE_RADIUS
        val bearingFromStart = startToPoint[1].toRadians()
        val bearingLine = startToEnd[1].toRadians()

        val crossTrackDistanceRadians =
            asin(sin(distanceFromStart) * sin(bearingFromStart - bearingLine))

        val radians = acos(cos(distanceFromStart) / abs(cos(crossTrackDistanceRadians)))
        val distance = radians * EARTH_AVERAGE_RADIUS * sign(cos(bearingLine - bearingFromStart))
        return Distance.meters(distance.toFloat())
    }

    fun getNearestPoint(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Coordinate {
        val alongTrack = getAlongTrackDistance(point, start, end)

        if (alongTrack.value < 0) {
            return start
        }

        val lineDistance = start.distanceTo(end)
        if (alongTrack.value > lineDistance) {
            return end
        }


        val bearing = start.bearingTo(end)
        return start.plus(alongTrack, bearing)
    }

    fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate {
        return from.plus(distance.toDouble(), bearing)
    }

    fun getPathDistance(points: List<Coordinate>, highAccuracy: Boolean = true): Distance {
        if (points.size < 2) {
            return Distance.meters(0f)
        }

        var distance = 0f
        for (i in 0..<points.lastIndex) {
            distance += points[i].distanceTo(points[i + 1], highAccuracy)
        }

        return Distance.meters(distance)
    }

    fun getElevationGain(elevations: List<Distance>): Distance {
        var sum = 0f

        if (elevations.isEmpty()) {
            return Distance.meters(0f)
        }

        for (i in 1..<elevations.size) {
            val current = elevations[i].meters().value
            val last = elevations[i - 1].meters().value
            val change = current - last
            if (change > 0) {
                sum += change
            }
        }
        return Distance.meters(sum)
    }

    fun getElevationLoss(elevations: List<Distance>): Distance {
        var sum = 0f

        if (elevations.isEmpty()) {
            return Distance.meters(0f)
        }

        for (i in 1..<elevations.size) {
            val current = elevations[i].meters().value
            val last = elevations[i - 1].meters().value
            val change = current - last
            if (change < 0) {
                sum += change
            }
        }
        return Distance.meters(sum)
    }
}