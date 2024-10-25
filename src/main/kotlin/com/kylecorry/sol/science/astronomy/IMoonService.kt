package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import java.time.Duration
import java.time.ZonedDateTime

interface IMoonService {
    fun getMoonEvents(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes

    fun getMoonAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float

    fun getMoonAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing

    fun getNextMoonset(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime?

    fun getNextMoonrise(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime?

    fun getMoonPhase(date: ZonedDateTime): MoonPhase

    fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean

    fun getMoonDistance(time: ZonedDateTime): Distance

    fun isSuperMoon(time: ZonedDateTime): Boolean

    /**
     * Gets the times the moon is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the moon is above the horizon or null if it is not above the horizon within approximately a day.
     */
    fun getMoonAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>?

    /**
     * The tilt of the illuminated fraction of the moon in degrees clockwise from the top of the moon.
     */
    fun getMoonTilt(time: ZonedDateTime, location: Coordinate): Float
}