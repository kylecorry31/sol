package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
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