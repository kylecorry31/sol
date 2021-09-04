package com.kylecorry.trailsensecore.astronomy

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import java.time.Duration
import java.time.ZonedDateTime

interface ISunService {

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

    fun getSunDistance(time: ZonedDateTime): Distance

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(date: ZonedDateTime, location: Coordinate): Double

}