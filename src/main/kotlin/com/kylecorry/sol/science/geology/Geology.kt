package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.clamp
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.*
import java.time.Instant
import kotlin.math.*

object Geology {

    const val GRAVITY = 9.81f
    const val EARTH_AVERAGE_RADIUS = 6371.2e3

    private val riskClassifier = AvalancheRiskClassifier()

    private val worldMagneticModel = SphericalHarmonics(
        WorldMagneticModel2025.G_COEFFICIENTS,
        WorldMagneticModel2025.H_COEFFICIENTS,
        baseTime = WorldMagneticModel2025.BASE_TIME,
        deltaGCoefficients = WorldMagneticModel2025.DELTA_G,
        deltaHCoefficients = WorldMagneticModel2025.DELTA_H
    )

    fun getGeomagneticDeclination(
        coordinate: Coordinate,
        altitude: Distance? = null,
        time: Instant = Instant.now()
    ): Angle {
        val geoField = worldMagneticModel.getVector(
            coordinate,
            altitude ?: Distance.meters(0f),
            time
        )
        return Angle.radians(atan2(geoField.y, geoField.x))
    }

    fun getGeomagneticInclination(
        coordinate: Coordinate,
        altitude: Distance? = null,
        time: Instant = Instant.now()
    ): Angle {
        val geoField = worldMagneticModel.getVector(
            coordinate,
            altitude ?: Distance.meters(0f),
            time
        )
        return Angle.radians(atan2(geoField.z, hypot(geoField.x, geoField.y)))
    }

    fun getGeomagneticField(
        coordinate: Coordinate,
        altitude: Distance? = null,
        time: Instant = Instant.now()
    ): Vector3 {
        val geoField = worldMagneticModel.getVector(
            coordinate,
            altitude ?: Distance.meters(0f),
            time
        )
        return Vector3(geoField.x * 0.001f, geoField.y * 0.001f, geoField.z * 0.001f)
    }

    fun getGravity(coordinate: Coordinate): Float {
        // Somigliana equation (IGF80)
        val ellipsoid = ReferenceEllipsoid.wgs84
        val ge = 9.78032677153489
        val k = 0.001931851353260676
        val e2 = ellipsoid.squaredEccentricity
        val sinLat2 = square(sinDegrees(coordinate.latitude))
        return (ge * (1 + k * sinLat2) / sqrt(1 - e2 * sinLat2)).toFloat()
    }

    /**
     * Determine the avalanche risk of a slope
     * @param inclination The inclination angle
     * @return The avalanche risk
     */
    fun getAvalancheRisk(inclination: Angle): AvalancheRisk {
        return riskClassifier.classify(inclination)
    }

    /**
     * Determines the grade (percent)
     * @param inclination The inclination angle
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(inclination: Angle): Float {
        if (inclination.degrees().value == 90f) {
            return Float.POSITIVE_INFINITY
        } else if (inclination.degrees().value == -90f) {
            return Float.NEGATIVE_INFINITY
        }

        return inclination.tan() * 100
    }

    fun getInclinationFromSlopeGrade(grade: Float): Angle {
        return Angle.radians(atan(grade / 100f))
    }


    fun getInclination(distance: Distance, elevationChange: Distance): Angle {
        return getInclinationFromSlopeGrade(getSlopeGrade(distance, elevationChange))
    }

    /**
     * Estimates the height of an object
     * @param distance The distance to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated height of the object
     */
    fun getHeightFromInclination(
        distance: Distance,
        bottomInclination: Angle,
        topInclination: Angle
    ): Distance {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Distance.from(Float.POSITIVE_INFINITY, distance.units)
        }

        return Distance.from(((up - down) * distance.value).absoluteValue, distance.units)
    }

    /**
     * Estimates the distance to an object
     * @param height The height to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated distance to the object
     */
    fun getDistanceFromInclination(
        height: Distance,
        bottomInclination: Angle,
        topInclination: Angle
    ): Distance {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Distance.from(0f, height.units)
        }
        return Distance.from((height.value / (up - down)).absoluteValue, height.units)
    }

    /**
     * Calculates the inclination from a unit angle
     * @param angle The angle, where 0 is the horizon (front), 90 is the sky (above), 180 is the horizon (behind), and 270 is the ground (below)
     */
    fun getInclination(angle: Angle): Angle {
        val angleDegrees = angle.degrees().value
        val resultDegrees = when (val wrappedAngle = wrap(angleDegrees, 0f, 360f)) {
            in 90f..270f -> 180f - wrappedAngle
            in 270f..360f -> wrappedAngle - 360f
            else -> wrappedAngle
        }
        return Angle.degrees(resultDegrees).convertTo(angle.units)
    }

    /**
     * Determines the grade (percent)
     * @param horizontal The horizontal distance
     * @param vertical The vertical distance
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(horizontal: Distance, vertical: Distance): Float {
        val y = vertical.meters().value
        val x = horizontal.meters().value

        if (SolMath.isZero(x) && y > 0f) {
            return Float.POSITIVE_INFINITY
        }

        if (SolMath.isZero(x) && y < 0f) {
            return Float.NEGATIVE_INFINITY
        }

        if (SolMath.isZero(x)) {
            return 0f
        }

        return y / x * 100
    }

    /**
     * Determines the grade (percent)
     * @param start The starting coordinate
     * @param startElevation The starting elevation
     * @param end The ending coordinate
     * @param endElevation The ending elevation
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(
        start: Coordinate,
        startElevation: Distance,
        end: Coordinate,
        endElevation: Distance
    ): Float {
        return getSlopeGrade(
            Distance.meters(start.distanceTo(end)),
            Distance.meters(endElevation.meters().value - startElevation.meters().value)
        )
    }

    fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean {
        return area.contains(coordinate)
    }

    fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing {
        return AzimuthCalculator.calculate(gravity, magneticField) ?: Bearing.from(0f)
    }

    fun getAltitude(pressure: Pressure, seaLevelPressure: Pressure): Distance {
        // TODO: Factor in temperature
        val hpa = pressure.hpa().value
        val seaHpa = seaLevelPressure.hpa().value
        val meters = 44330.0 * (1 - (hpa / seaHpa).toDouble().pow(1 / 5.255))
        return Distance.meters(meters.toFloat())
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
        selfToReferenceBearingA: Angle,
        referenceB: Coordinate,
        selfToReferenceBearingB: Angle
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
            a1 = selfToReferenceBearingA.radians().inverse().value - initialBearing
            a2 = 2 * Math.PI - finalBearing - selfToReferenceBearingB.radians().inverse().value
        } else {
            a1 = selfToReferenceBearingA.radians().inverse().value - (2 * Math.PI - initialBearing)
            a2 = finalBearing - selfToReferenceBearingB.radians().inverse().value
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
            ) * selfToReferenceBearingA.inverse().cos()
        )
        val deltaP3Long = atan2(
            selfToReferenceBearingA.inverse().sin() * sin(angularDist13) * cosDegrees(
                referenceA.latitude
            ),
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
        referenceAToDestinationBearing: Angle,
        referenceB: Coordinate,
        referenceBToDestinationBearing: Angle
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
        distanceTravelled: Distance,
        bearingToLast: Angle
    ): Coordinate {
        return lastLocation.plus(distanceTravelled.meters(), bearingToLast.inverse())
    }

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Angle = Angle.degrees(0f),
        useTrueNorth: Boolean = true,
        highAccuracy: Boolean = true
    ): NavigationVector {
        val results = if (highAccuracy) {
            DistanceCalculator.vincenty(from, to)
        } else {
            DistanceCalculator.haversine(from, to, EARTH_AVERAGE_RADIUS)
        }

        val declinationAdjustment = if (useTrueNorth) {
            Angle.from(0f, declination.units)
        } else {
            Angle.from(-declination.value, declination.units)
        }

        return NavigationVector(
            Angle.degrees(results[1]).plus(declinationAdjustment).normalized().convertTo(declination.units),
            Distance.meters(results[0])
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

        val startToPoint = DistanceCalculator.haversine(start, point, EARTH_AVERAGE_RADIUS)
        val startToEnd = DistanceCalculator.haversine(start, end, EARTH_AVERAGE_RADIUS)

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

        val startToPoint = DistanceCalculator.haversine(start, point, EARTH_AVERAGE_RADIUS)
        val startToEnd = DistanceCalculator.haversine(start, end, EARTH_AVERAGE_RADIUS)

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

    fun destination(from: Coordinate, distance: Distance, bearing: Angle): Coordinate {
        return from.plus(distance, bearing)
    }

    fun getPathDistance(points: List<Coordinate>, highAccuracy: Boolean): Distance {
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