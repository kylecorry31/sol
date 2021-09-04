package com.kylecorry.trailsensecore.domain.navigation

import android.location.Location
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.math.*
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

    override fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate {
        return from.plus(distance.toDouble(), bearing)
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

    override fun getPathDistance(points: List<Coordinate>): Distance {
        if (points.size < 2) {
            return Distance.meters(0f)
        }

        var distance = 0f
        for (i in 0 until points.lastIndex) {
            distance += points[i].distanceTo(points[i + 1])
        }

        return Distance.meters(distance)
    }
}