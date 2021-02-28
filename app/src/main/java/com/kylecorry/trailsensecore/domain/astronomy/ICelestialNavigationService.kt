package com.kylecorry.trailsensecore.domain.astronomy

import java.time.Instant
import java.time.LocalTime

interface ICelestialNavigationService {

    // Latitude only
    fun getLatitudeFromPolaris(polarisAltitude: Float): Double
    fun getLatitudeFromNoon(sunAltitude: Float, noon: Instant, inNorthernHemisphere: Boolean): Double
    fun getLatitudeFromShadow(shadowLength: Float, objectLength: Float, utc: Instant, isAfternoon: Boolean): Double
    fun getLatitudeFromSun(sunLowerLimbAltitude: Float, utc: Instant, isAfternoon: Boolean): Double

    // Longitude only
    fun getLongitudeFromNoon(noon: Instant): Double
    fun getLongitudeFromSundial(localSolarTime: LocalTime, instant: Instant): Double

    // Helpers
    fun getSunAltitudeFromShadow(objectHeight: Float, shadowLength: Float): Float
}