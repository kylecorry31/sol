package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import java.time.Instant
import java.time.LocalTime

interface ICelestialNavigationService {

    // Both
    fun triangulate(pointA: Coordinate, bearingA: Bearing, pointB: Coordinate, bearingB: Bearing): Coordinate
    fun triangulate(pointA: Coordinate, bearingA: Bearing, pointB: Coordinate, bearingB: Bearing, pointC: Coordinate, bearingC: Bearing): Coordinate
    fun deadReckon(lastLocation: Coordinate, distanceTravelled: Float, bearingToLast: Bearing)

    // Latitude only
    fun getLatitudeFromPolaris(polarisAltitude: Float): Double
    fun getLatitudeFromNoon(sunAltitude: Float, noon: Instant, inNorthernHemisphere: Boolean): Double
    fun getLatitudeFromShadow(shadowLength: Float, objectLength: Float, utc: Instant, isAfternoon: Boolean): Double
    fun getLatitudeFromSun(sunLowerLimbAltitude: Float, utc: Instant, isAfternoon: Boolean): Double

    // Longitude only
    fun getLongitudeFromNoon(noon: Instant): Double
    fun getLongitudeFromSundial(localSolarTime: LocalTime, utc: Instant): Double

    // Helpers
    fun getSunAltitudeFromShadow(objectHeight: Float, shadowLength: Float): Float
}