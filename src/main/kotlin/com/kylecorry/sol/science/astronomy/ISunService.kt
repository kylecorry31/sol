package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity
import java.time.Duration
import java.time.ZonedDateTime

interface ISunService {

    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes

    fun getSunAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float

    fun getSunAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing

    fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime?

    fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime?

    fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean

    fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Duration

    fun getSunDistance(time: ZonedDateTime): Quantity<Distance>

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Double

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        tilt: Float,
        azimuth: Bearing,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Double

    /**
     * Gets the times the sun is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param mode The mode to use for calculating sun times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the sun is above the horizon or null if it is not above the horizon within approximately a day.
     */
    fun getSunAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>?

}