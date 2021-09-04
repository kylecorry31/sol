package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import java.time.ZonedDateTime

interface IMoonService {
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

    fun getMoonDistance(time: ZonedDateTime): Distance

    fun isSuperMoon(time: ZonedDateTime): Boolean
}