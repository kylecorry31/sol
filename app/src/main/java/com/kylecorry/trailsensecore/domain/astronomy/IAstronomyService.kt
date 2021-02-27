package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.geo.CompassDirection
import java.time.LocalDate
import java.time.ZonedDateTime

interface IAstronomyService {

    // SUN
    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes

    fun getSunAltitude(time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): Float
    fun getSunAzimuth(time: ZonedDateTime, location: Coordinate): Bearing

    fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false
    ): ZonedDateTime?

    fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false
    ): ZonedDateTime?

    fun isSunUp(time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): Boolean

    // SOLAR PANELS
    fun getBestSolarPanelPositionForDay(date: ZonedDateTime, location: Coordinate): SolarPanelPosition
    fun getBestSolarPanelPositionForTime(time: ZonedDateTime, location: Coordinate): SolarPanelPosition

    // MOON
    fun getMoonEvents(date: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): RiseSetTransitTimes
    fun getMoonAltitude(time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): Float
    fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing
    fun getNextMoonset(time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): ZonedDateTime?
    fun getNextMoonrise(time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): ZonedDateTime?
    fun getMoonPhase(date: ZonedDateTime): MoonPhase

    fun isMoonUp(time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): Boolean

    // Other bodies
    fun getCelestialObjectEvents(body: CelestialObject, date: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): RiseSetTransitTimes
    fun getCelestialObjectAltitude(body: CelestialObject, time: ZonedDateTime, location: Coordinate, withRefraction: Boolean = false): Float
    fun getCelestialObjectAzimuth(body: CelestialObject, time: ZonedDateTime, location: Coordinate): Bearing

}