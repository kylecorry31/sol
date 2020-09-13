package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.Bearing
import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import java.time.ZonedDateTime

interface IAstronomyService {

    // SUN
    fun getSunEvents(date: ZonedDateTime, location: Coordinate): RiseSetTransitTimes {
        return getSunEvents(date, location, SunTimesMode.Actual)
    }

    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): RiseSetTransitTimes

    fun getSunAltitude(time: ZonedDateTime, location: Coordinate): Float
    fun getSunAzimuth(time: ZonedDateTime, location: Coordinate): Bearing

    fun getNextSunset(time: ZonedDateTime, location: Coordinate): ZonedDateTime? {
        return getNextSunset(time, location, SunTimesMode.Actual)
    }

    fun getNextSunrise(time: ZonedDateTime, location: Coordinate): ZonedDateTime? {
        return getNextSunrise(time, location, SunTimesMode.Actual)
    }

    fun getNextSunset(time: ZonedDateTime, location: Coordinate, mode: SunTimesMode): ZonedDateTime?
    fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode
    ): ZonedDateTime?

    fun isSunUp(time: ZonedDateTime, location: Coordinate): Boolean {
        return getSunAltitude(time, location) > 0
    }


    // MOON
    fun getMoonEvents(date: ZonedDateTime, location: Coordinate): RiseSetTransitTimes
    fun getMoonAltitude(time: ZonedDateTime, location: Coordinate): Float
    fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing
    fun getNextMoonset(time: ZonedDateTime, location: Coordinate): ZonedDateTime?
    fun getNextMoonrise(time: ZonedDateTime, location: Coordinate): ZonedDateTime?
    fun getMoonPhase(date: ZonedDateTime): MoonPhase

    fun isMoonUp(time: ZonedDateTime, location: Coordinate): Boolean {
        return getMoonAltitude(time, location) > 0
    }

}