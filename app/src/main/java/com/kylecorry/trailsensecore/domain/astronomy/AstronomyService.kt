package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.Bearing
import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.time.DateUtils
import java.time.ZonedDateTime

class AstronomyService : IAstronomyService {

    override fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): RiseSetTransitTimes {
        return when (mode) {
            SunTimesMode.Actual -> Astro.getSunTimes(date, location, -0.8333)
            SunTimesMode.Civil -> Astro.getSunTimes(date, location, -6.0)
            SunTimesMode.Nautical -> Astro.getSunTimes(date, location, -12.0)
            SunTimesMode.Astronomical -> Astro.getSunTimes(date, location, -18.0)
        }
    }

    override fun getSunAltitude(time: ZonedDateTime, location: Coordinate): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        )
        return Astro.altitude(hourAngle, location.latitude, solarCoordinates.declination).toFloat()
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
        mode: SunTimesMode
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode)
        val tomorrow = getSunEvents(time.plusDays(1), location, mode)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonEvents(date: ZonedDateTime, location: Coordinate): RiseSetTransitTimes {
        return Astro.getMoonTimes(date, location)
    }

    override fun getMoonAltitude(time: ZonedDateTime, location: Coordinate): Float {
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val lunarCoordinates = Astro.lunarCoordinates(jd)
        val hourAngle = Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            lunarCoordinates.rightAscension
        )
        return Astro.altitude(hourAngle, location.latitude, lunarCoordinates.declination).toFloat()
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

    override fun getNextMoonset(time: ZonedDateTime, location: Coordinate): ZonedDateTime? {
        val today = getMoonEvents(time, location)
        val tomorrow = getMoonEvents(time.plusDays(1), location)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.set, tomorrow.set)
        )
    }

    override fun getNextMoonrise(time: ZonedDateTime, location: Coordinate): ZonedDateTime? {
        val today = getMoonEvents(time, location)
        val tomorrow = getMoonEvents(time.plusDays(1), location)
        return DateUtils.getClosestFutureTime(
            time,
            listOf(today.rise, tomorrow.rise)
        )
    }

    override fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return Astro.getMoonPhase(date)
    }

}