package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceCalculator
import kotlin.math.*

class GeologyService : IGeologyService {
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

    override fun getHeightFromInclination(
        distance: Distance,
        bottomInclination: Float,
        topInclination: Float
    ): Distance {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Distance(Float.POSITIVE_INFINITY, distance.units)
        }

        return Distance(((up - down) * distance.distance).absoluteValue, distance.units)
    }

    override fun getDistanceFromInclination(
        height: Distance,
        bottomInclination: Float,
        topInclination: Float
    ): Distance {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Distance(0f, height.units)
        }
        return Distance((height.distance / (up - down)).absoluteValue, height.units)
    }

    override fun getInclination(angle: Float): Float {
        return when (val wrappedAngle = wrap(angle, 0f, 360f)) {
            in 90f..270f -> 180f - wrappedAngle
            in 270f..360f -> wrappedAngle - 360f
            else -> wrappedAngle
        }
    }

    override fun containedByArea(coordinate: Coordinate, area: IGeoArea): Boolean {
        return area.contains(coordinate)
    }

    override fun getAzimuth(gravity: Vector3, magneticField: Vector3): Bearing {
        return AzimuthCalculator.calculate(gravity, magneticField) ?: Bearing(0f)
    }

    override fun getRegion(coordinate: Coordinate): Region {
        return when {
            coordinate.latitude.absoluteValue >= 66.5 -> Region.Polar
            coordinate.latitude.absoluteValue >= 23.5 -> Region.Temperate
            else -> Region.Tropical
        }
    }

    override fun getMapDistance(
        measurement: Distance,
        scaleFrom: Distance,
        scaleTo: Distance
    ): Distance {
        val scaledMeasurement = measurement.convertTo(scaleFrom.units)
        return Distance(
            scaleTo.distance * scaledMeasurement.distance / scaleFrom.distance,
            scaleTo.units
        )
    }

    override fun getMapDistance(measurement: Distance, ratioFrom: Float, ratioTo: Float): Distance {
        return Distance(ratioTo * measurement.distance / ratioFrom, measurement.units)
    }

    override fun getBounds(points: List<Coordinate>): CoordinateBounds {
        return CoordinateBounds.from(points)
    }

    override fun triangulate(
        pointA: Coordinate,
        bearingA: Bearing,
        pointB: Coordinate,
        bearingB: Bearing
    ): Coordinate? {
        val deltaLat = pointA.latitude - pointB.latitude
        val deltaLng = pointA.longitude - pointB.longitude
        val angularDist = 2 * asin(
            sqrt(
                sinDegrees(deltaLat / 2) * sinDegrees(deltaLat / 2) + cosDegrees(pointA.latitude) * cosDegrees(
                    pointB.latitude
                ) * sinDegrees(deltaLng / 2) * sinDegrees(deltaLng / 2)
            )
        )

        val initialBearing = acos(
            (sinDegrees(pointB.latitude) - sinDegrees(pointA.latitude) * cos(angularDist)) / (sin(
                angularDist
            ) * cosDegrees(pointA.latitude))
        )
        val finalBearing = acos(
            (sinDegrees(pointA.latitude) - sinDegrees(pointB.latitude) * cos(angularDist)) / (sin(
                angularDist
            ) * cosDegrees(pointB.latitude))
        )

        val a1: Double
        val a2: Double
        if (sinDegrees(pointB.longitude - pointA.longitude) > 0) {
            a1 = bearingA.inverse().value.toDouble().toRadians() - initialBearing
            a2 = 2 * Math.PI - finalBearing - bearingB.inverse().value.toDouble().toRadians()
        } else {
            a1 = bearingA.inverse().value.toDouble().toRadians() - (2 * Math.PI - initialBearing)
            a2 = finalBearing - bearingB.inverse().value.toDouble().toRadians()
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
            sinDegrees(pointA.latitude) * cos(angularDist13) + cosDegrees(pointA.latitude) * sin(
                angularDist13
            ) * cosDegrees(bearingA.inverse().value.toDouble())
        )
        val deltaP3Long = atan2(
            sinDegrees(bearingA.inverse().value.toDouble()) * sin(angularDist13) * cosDegrees(pointA.latitude),
            cos(angularDist13) - sinDegrees(pointA.latitude) * sin(p3Lat)
        )
        val p3Lng = pointA.longitude.toRadians() + deltaP3Long
        return Coordinate(p3Lat.toDegrees(), p3Lng.toDegrees())
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

    override fun getAlongTrackDistance(
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

    override fun getNearestPoint(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Coordinate {
        val alongTrack = getAlongTrackDistance(point, start, end)

        if (alongTrack.distance < 0) {
            return start
        }

        val lineDistance = start.distanceTo(end)
        if (alongTrack.distance > lineDistance) {
            return end
        }


        val bearing = start.bearingTo(end)
        return start.plus(alongTrack, bearing)
    }

    override fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate {
        return from.plus(distance.toDouble(), bearing)
    }

    override fun getPathDistance(points: List<Coordinate>, highAccuracy: Boolean): Distance {
        if (points.size < 2) {
            return Distance.meters(0f)
        }

        var distance = 0f
        for (i in 0 until points.lastIndex) {
            distance += points[i].distanceTo(points[i + 1], highAccuracy)
        }

        return Distance.meters(distance)
    }

    override fun getElevationGain(elevations: List<Distance>): Distance {
        var sum = 0f

        if (elevations.isEmpty()) {
            return Distance.meters(0f)
        }

        for (i in 1 until elevations.size) {
            val current = elevations[i].meters().distance
            val last = elevations[i - 1].meters().distance
            val change = current - last
            if (change > 0) {
                sum += change
            }
        }
        return Distance.meters(sum)
    }

    override fun getElevationLoss(elevations: List<Distance>): Distance {
        var sum = 0f

        if (elevations.isEmpty()) {
            return Distance.meters(0f)
        }

        for (i in 1 until elevations.size) {
            val current = elevations[i].meters().distance
            val last = elevations[i - 1].meters().distance
            val change = current - last
            if (change < 0) {
                sum += change
            }
        }
        return Distance.meters(sum)
    }

    companion object {
        const val GRAVITY = 9.81f
        const val EARTH_AVERAGE_RADIUS = 6371.2e3
    }
}