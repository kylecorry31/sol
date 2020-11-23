package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.geo.CompassDirection
import com.kylecorry.trailsensecore.domain.time.DateUtils
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.max

class AstronomyService : IAstronomyService {

    override fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): RiseSetTransitTimes {

        val altitude = when (mode) {
            SunTimesMode.Actual -> -0.8333
            SunTimesMode.Civil -> -6.0
            SunTimesMode.Nautical -> -12.0
            SunTimesMode.Astronomical -> -18.0
        }

        return Astro.getSunTimes(date, location, altitude, withRefraction)
    }

    override fun getSunAltitude(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        )
        return Astro.altitude(
            hourAngle,
            location.latitude,
            solarCoordinates.declination,
            withRefraction
        )
            .toFloat()
    }

    override fun getSunAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        )
        return Bearing(
            Astro.azimuth(hourAngle, location.latitude, solarCoordinates.declination).toFloat()
        )
    }

    override fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean
    ): Boolean {
        return getSunAltitude(time, location, withRefraction) > 0
    }

    override fun getOptimalSolarTilt(date: ZonedDateTime, location: Coordinate): Float {
        var maximum = 0f
        val interval = 5L

        var time = date.withHour(0).withMinute(0).withSecond(0)

        while(time.toLocalDate() == date.toLocalDate()){
            val altitude = getSunAltitude(time, location, true)
            if (altitude >= 0){
                maximum = max(altitude, maximum)
            }
            time = time.plusMinutes(interval)
        }

        return 90f - maximum
    }

    override fun getOptimalSolarDirection(location: Coordinate): Bearing {
        return if (location.latitude > 0){
            Bearing.from(CompassDirection.South)
        } else {
            Bearing.from(CompassDirection.North)
        }
    }

    override fun getMoonEvents(
        date: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): RiseSetTransitTimes {
        return Astro.getMoonTimes(date, location, withRefraction = withRefraction)
    }

    override fun getMoonAltitude(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val lunarCoordinates = Astro.lunarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            lunarCoordinates.rightAscension
        )
        return Astro.altitude(
            hourAngle,
            location.latitude,
            lunarCoordinates.declination,
            withRefraction
        )
            .toFloat()
    }

    override fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val lunarCoordinates = Astro.lunarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            lunarCoordinates.rightAscension
        )
        return Bearing(
            Astro.azimuth(hourAngle, location.latitude, lunarCoordinates.declination).toFloat()
        )
    }

    override fun getNextMoonset(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction)
        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextMoonrise(
        time: ZonedDateTime, location: Coordinate,
        withRefraction: Boolean
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction)
        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return Astro.getMoonPhase(date)
    }

    override fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean
    ): Boolean {
        return getMoonAltitude(time, location, withRefraction) > 0
    }

}