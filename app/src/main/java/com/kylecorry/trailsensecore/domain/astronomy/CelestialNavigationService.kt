package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.math.toDegrees
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.atan

class CelestialNavigationService: ICelestialNavigationService {

    private val astronomyService = AstronomyService()

    override fun getLatitudeFromPolaris(polarisAltitude: Float): Double {
        return polarisAltitude.toDouble()
    }

    override fun getLatitudeFromNoon(sunAltitude: Float, noon: Instant, inNorthernHemisphere: Boolean): Double {
        val ut = Astro.ut(noon.atZone(ZoneId.of("UTC")))
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val declination = solarCoordinates.declination

        return if (inNorthernHemisphere) {
            90.0 - sunAltitude.toDouble() + declination
        } else {
            -(90.0 - sunAltitude.toDouble() - declination)
        }
    }

    override fun getLatitudeFromShadow(
        shadowLength: Float,
        objectLength: Float,
        utc: Instant,
        isAfternoon: Boolean
    ): Double {
        return getLatitudeFromSun(getSunAltitudeFromShadow(objectLength, shadowLength), utc, isAfternoon)
    }

    override fun getLatitudeFromSun(
        sunLowerLimbAltitude: Float,
        utc: Instant,
        isAfternoon: Boolean
    ): Double {
        TODO("Not yet implemented")
    }

    override fun getLongitudeFromNoon(noon: Instant): Double {
        return getLongitudeFromSundial(LocalTime.NOON, noon)
    }

    override fun getLongitudeFromSundial(localSolarTime: LocalTime, instant: Instant): Double {
        val utc = instant.atZone(ZoneId.of("UTC"))
        val primeMeridian = Coordinate(51.4769, 0.0)
        val solarNoon = astronomyService.getSunEvents(utc, primeMeridian).transit
        val utcNoon = solarNoon?.toLocalTime() ?: LocalTime.NOON
        val noonDiff = Duration.between(utcNoon, LocalTime.NOON)
        return -getLongitudeFromUtcDiff(localSolarTime, utc.plus(noonDiff).toLocalTime())
    }

    override fun getSunAltitudeFromShadow(objectHeight: Float, shadowLength: Float): Float {
        return atan(objectHeight / shadowLength).toDegrees()
    }

    private fun getLongitudeFromUtcDiff(local: LocalTime, utc: LocalTime): Double {
        val longitudePerHour = 15.0
        val millisToHours = 1.0 / (1000f * 60f * 60f)
        val hourDiff = Duration.between(local, utc).toMillis() * millisToHours
        return hourDiff * longitudePerHour
    }
}