package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.IEclipseService
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.time.Season
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

interface IAstronomyService : IEclipseService {

    // SUN
    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes

    fun getSunAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float

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

    fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode = SunTimesMode.Actual
    ): Duration

    // SOLAR PANELS
    fun getBestSolarPanelPositionForRestOfDay(
        start: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition

    fun getBestSolarPanelPositionForDay(
        date: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition

    fun getBestSolarPanelPositionForTime(
        time: ZonedDateTime,
        location: Coordinate
    ): SolarPanelPosition

    // MOON
    fun getMoonEvents(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): RiseSetTransitTimes

    fun getMoonAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Float

    fun getMoonAzimuth(time: ZonedDateTime, location: Coordinate): Bearing
    fun getNextMoonset(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): ZonedDateTime?

    fun getNextMoonrise(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): ZonedDateTime?

    fun getMoonPhase(date: ZonedDateTime): MoonPhase

    fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false
    ): Boolean

    fun getMoonDistance(time: Instant): Distance

    fun isSuperMoon(time: Instant): Boolean

    // Meteor showers
    fun getMeteorShower(location: Coordinate, date: ZonedDateTime): MeteorShowerPeak?
    fun getMeteorShowerAltitude(shower: MeteorShower, location: Coordinate, time: Instant): Float
    fun getMeteorShowerAzimuth(shower: MeteorShower, location: Coordinate, time: Instant): Bearing

    // Seasons
    fun getAstronomicalSeason(location: Coordinate, date: ZonedDateTime): Season

}