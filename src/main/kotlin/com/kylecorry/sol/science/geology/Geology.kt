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
import kotlin.math.*

object Geology : IGeologyService {

    const val GRAVITY = 9.81f
    const val EARTH_AVERAGE_RADIUS = 6371.2e3

    private val riskClassifier = AvalancheRiskClassifier()

    override fun getGeomagneticDeclination(
        coordinate: Coordinate,
        altitude: Float?,
        time: Long
    ): Float {
        val geoField = GeomagneticField2020(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return geoField.declination
    }

    override fun getGeomagneticInclination(
        coordinate: Coordinate,
        altitude: Float?,
        time: Long
    ): Float {
        val geoField = GeomagneticField2020(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return geoField.inclination
    }

    override fun getGeomagneticField(
        coordinate: Coordinate,
        altitude: Float?,
        time: Long
    ): Vector3 {
        val geoField = GeomagneticField2020(
            coordinate.latitude.toFloat(),
            coordinate.longitude.toFloat(),
            altitude ?: 0f,
            time
        )
        return Vector3(geoField.x * 0.001f, geoField.y * 0.001f, geoField.z * 0.001f)
    }

    override fun getGravity(coordinate: Coordinate): Float {
        // Somigliana equation (IGF80)
        val ellipsoid = ReferenceEllipsoid.wgs84
        val ge = 9.78032677153489
        val k = 0.001931851353260676
        val e2 = ellipsoid.squaredEccentricity
        val sinLat2 = square(sinDegrees(coordinate.latitude))
        return (ge * (1 + k * sinLat2) / sqrt(1 - e2 * sinLat2)).toFloat()
    }

    override fun getAvalancheRisk(inclination: Float): AvalancheRisk {
        return riskClassifier.classify(inclination)
    }

    override fun getSlopeGrade(inclination: Float): Float {
        if (inclination == 90f) {
            return Float.POSITIVE_INFINITY
        } else if (inclination == -90f) {
            return Float.NEGATIVE_INFINITY
        }

        return SolMath.tanDegrees(inclination) * 100
    }

    override fun getInclinationFromSlopeGrade(grade: Float): Float {
        return atan(grade / 100f).toDegrees()
    }

    override fun getInclination(distance: Quantity<Distance>, elevationChange: Quantity<Distance>): Float {
        return getInclinationFromSlopeGrade(getSlopeGrade(distance, elevationChange))
    }

    override fun getHeightFromInclination(
        distance: Quantity<Distance>,
        bottomInclination: Float,
        topInclination: Float
    ): Quantity<Distance> {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Quantity(Float.POSITIVE_INFINITY, distance.units)
        }

        return Quantity(((up - down) * distance.amount).absoluteValue, distance.units)
    }

    override fun getDistanceFromInclination(
        height: Quantity<Distance>,
        bottomInclination: Float,
        topInclination: Float
    ): Quantity<Distance> {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Quantity(0f, height.units)
        }
        return Quantity((height.amount / (up - down)).absoluteValue, height.units)
    }

    override fun getInclination(angle: Float): Float {
        return when (val wrappedAngle = wrap(angle, 0f, 360f)) {
            in 90f..270f -> 180f - wrappedAngle
            in 270f..360f -> wrappedAngle - 360f
            else -> wrappedAngle
        }
    }

    override fun getSlopeGrade(horizontal: Quantity<Distance>, vertical: Quantity<Distance>): Float {
        val y = vertical.meters().amount
        val x = horizontal.meters().amount

        if (x == 0f && y > 0f) {
            return Float.POSITIVE_INFINITY
        }

        if (x == 0f && y < 0f) {
            return Float.NEGATIVE_INFINITY
        }

        if (x == 0f) {
            return 0f
        }

        return y / x * 100
    }

    override fun getSlopeGrade(
        start: Coordinate,
        startElevation: Quantity<Distance>,
        end: Coordinate,
        endElevation: Quantity<Distance>
    ): Float {
        return getSlopeGrade(
            Distance.meters(start.distanceTo(end)),
            Distance.meters(endElevation.meters().amount - startElevation.meters().amount)
        )
    }

    override fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean {
        return area.contains(coordinate)
    }

    override fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing {
        return AzimuthCalculator.calculate(gravity, magneticField) ?: Bearing(0f)
    }

    override fun getAltitude(pressure: Pressure, seaLevelPressure: Pressure): Quantity<Distance> {
        // TODO: Factor in temperature
        val hpa = pressure.hpa().pressure
        val seaHpa = seaLevelPressure.hpa().pressure
        val meters = 44330.0 * (1 - (hpa / seaHpa).toDouble().pow(1 / 5.255))
        return Distance.meters(meters.toFloat())
    }

    override fun getRegion(coordinate: Coordinate): Region {
        return when {
            coordinate.latitude.absoluteValue >= 66.5 -> Region.Polar
            coordinate.latitude.absoluteValue >= 23.5 -> Region.Temperate
            else -> Region.Tropical
        }
    }

    override fun getMapDistance(
        measurement: Quantity<Distance>,
        scaleFrom: Quantity<Distance>,
        scaleTo: Quantity<Distance>
    ): Quantity<Distance> {
        val scaledMeasurement = measurement.convertTo(scaleFrom.units)
        return Quantity(
            scaleTo.amount * scaledMeasurement.amount / scaleFrom.amount,
            scaleTo.units
        )
    }

    override fun getMapDistance(measurement: Quantity<Distance>, ratioFrom: Float, ratioTo: Float): Quantity<Distance> {
        return Quantity(ratioTo * measurement.amount / ratioFrom, measurement.units)
    }

    override fun getBounds(points: List<Coordinate>): CoordinateBounds {
        return CoordinateBounds.from(points)
    }

    override fun triangulateSelf(
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

    override fun triangulateDestination(
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

    override fun deadReckon(
        lastLocation: Coordinate,
        distanceTravelled: Float,
        bearingToLast: Bearing
    ): Coordinate {
        return lastLocation.plus(distanceTravelled.toDouble(), bearingToLast.inverse())
    }

    override fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float,
        useTrueNorth: Boolean,
        highAccuracy: Boolean
    ): NavigationVector {
        val results = if (highAccuracy) {
            DistanceCalculator.vincenty(from, to)
        } else {
            DistanceCalculator.haversine(from, to, EARTH_AVERAGE_RADIUS)
        }

        val declinationAdjustment = if (useTrueNorth) {
            0f
        } else {
            -declination
        }

        return NavigationVector(
            Bearing(results[1]).withDeclination(declinationAdjustment),
            results[0]
        )
    }

    override fun getCrossTrackDistance(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Quantity<Distance> {
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

    override fun getAlongTrackDistance(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Quantity<Distance> {

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

    override fun getNearestPoint(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Coordinate {
        val alongTrack = getAlongTrackDistance(point, start, end)

        if (alongTrack.amount < 0) {
            return start
        }

        val lineDistance = start.distanceTo(end)
        if (alongTrack.amount > lineDistance) {
            return end
        }


        val bearing = start.bearingTo(end)
        return start.plus(alongTrack, bearing)
    }

    override fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate {
        return from.plus(distance.toDouble(), bearing)
    }

    override fun getPathDistance(points: List<Coordinate>, highAccuracy: Boolean): Quantity<Distance> {
        if (points.size < 2) {
            return Distance.meters(0f)
        }

        var distance = 0f
        for (i in 0..<points.lastIndex) {
            distance += points[i].distanceTo(points[i + 1], highAccuracy)
        }

        return Distance.meters(distance)
    }

    override fun getElevationGain(elevations: List<Quantity<Distance>>): Quantity<Distance> {
        var sum = 0f

        if (elevations.isEmpty()) {
            return Distance.meters(0f)
        }

        for (i in 1..<elevations.size) {
            val current = elevations[i].meters().amount
            val last = elevations[i - 1].meters().amount
            val change = current - last
            if (change > 0) {
                sum += change
            }
        }
        return Distance.meters(sum)
    }

    override fun getElevationLoss(elevations: List<Quantity<Distance>>): Quantity<Distance> {
        var sum = 0f

        if (elevations.isEmpty()) {
            return Distance.meters(0f)
        }

        for (i in 1..<elevations.size) {
            val current = elevations[i].meters().amount
            val last = elevations[i - 1].meters().amount
            val change = current - last
            if (change < 0) {
                sum += change
            }
        }
        return Distance.meters(sum)
    }
}