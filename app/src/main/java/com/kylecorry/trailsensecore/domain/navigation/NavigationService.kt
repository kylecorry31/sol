package com.kylecorry.trailsensecore.domain.navigation

import android.location.Location
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.math.*
import java.time.Duration
import kotlin.math.*

class NavigationService : INavigationService {
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
        useTrueNorth: Boolean
    ): NavigationVector {
        val results = FloatArray(3)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)

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

    override fun navigate(
        from: Position,
        to: Beacon,
        declination: Float,
        usingTrueNorth: Boolean
    ): NavigationVector {
        val originalVector = navigate(from.location, to.coordinate, declination, usingTrueNorth)
        val altitudeChange = if (to.elevation != null) to.elevation - from.altitude else null
        return originalVector.copy(altitudeChange = altitudeChange)
    }

    override fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate {
        return from.plus(distance.toDouble(), bearing)
    }

    override fun eta(from: Position, to: Beacon, nonLinear: Boolean): Duration {
        val speed =
            if (from.speed < 3) clamp(from.speed, 0.89408f, 1.78816f) else from.speed
        val elevationGain =
            max(if (to.elevation == null) 0f else (to.elevation - from.altitude), 0f)
        val distance =
            from.location.distanceTo(to.coordinate) * (if (nonLinear) PI.toFloat() / 2f else 1f)

        val baseTime = distance / speed
        val elevationSeconds = (elevationGain / 300f) * 30f * 60f

        return Duration.ofSeconds(baseTime.toLong()).plusSeconds(elevationSeconds.toLong())
    }

    override fun nearby(
        location: Coordinate,
        beacons: List<Beacon>,
        maxDistance: Float
    ): List<Beacon> {
        return beacons
            .asSequence()
            .map { Pair(it, location.distanceTo(it.coordinate)) }
            .filter { it.second <= maxDistance }
            .sortedBy { it.second }
            .map { it.first }
            .toList()
    }

    override fun getPaceDistance(paces: Int, paceLength: Distance): Distance {
        return Distance(paces * paceLength.distance, paceLength.units)
    }

    override fun getPaces(steps: Int): Int {
        return steps / 2
    }

    override fun getPaceLength(paces: Int, distanceTravelled: Distance): Distance {
        return Distance(paces / distanceTravelled.distance, distanceTravelled.units)
    }
}