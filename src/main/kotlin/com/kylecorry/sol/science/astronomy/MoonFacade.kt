package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.rst.RobustRiseSetTransitTimeCalculator
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import java.time.Duration
import java.time.ZonedDateTime

internal object MoonFacade {
    private val moon = Moon()
    private val riseSetTransitCalculator = RobustRiseSetTransitTimeCalculator()

    fun getMoonEvents(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes {
        return riseSetTransitCalculator.calculate(
            moon,
            date,
            location,
            0.125,
            withRefraction,
            withParallax
        )
    }

    fun getMoonAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return AstroUtils.getAltitude(
            moon,
            time.toUniversalTime(),
            location,
            withRefraction,
            withParallax
        )
    }

    fun getMoonAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        return AstroUtils.getAzimuth(moon, time.toUniversalTime(), location, withParallax)
    }

    fun getNextMoonset(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction, withParallax)
        if (today.set != null && today.set > time) {
            return today.set
        }

        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction, withParallax)
        if (tomorrow.set != null && tomorrow.set > time) {
            return tomorrow.set
        }

        return null
    }

    fun getNextMoonrise(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        val today = getMoonEvents(time, location, withRefraction, withParallax)
        if (today.rise != null && today.rise > time) {
            return today.rise
        }

        val tomorrow = getMoonEvents(time.plusDays(1), location, withRefraction, withParallax)
        if (tomorrow.rise != null && tomorrow.rise > time) {
            return tomorrow.rise
        }

        return null
    }

    fun getMoonPhase(date: ZonedDateTime): MoonPhase {
        return moon.getPhase(date.toUniversalTime())
    }

    fun isMoonUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean {
        return getMoonAltitude(time, location, withRefraction, withParallax) > 0
    }

    fun getMoonDistance(time: ZonedDateTime): Distance {
        return moon.getDistance(time.toUniversalTime())
    }

    fun isSuperMoon(time: ZonedDateTime): Boolean {
        val phase = getMoonPhase(time)
        if (phase.phase != MoonTruePhase.Full) {
            return false
        }
        val distance = getMoonDistance(time)
        return distance.convertTo(DistanceUnits.Kilometers).value <= 360000f
    }

    /**
     * Gets the times the moon is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the moon is above the horizon or null if it is not above the horizon
     * within approximately a day.
     */
    fun getMoonAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>? {
        return AboveHorizonFacade.getAboveHorizonTimes(
            location,
            time,
            nextRiseOffset,
            { loc, t -> isMoonUp(t, loc, withRefraction, withParallax) },
            { loc, t -> getMoonEvents(t, loc, withRefraction, withParallax) }
        )
    }

    /**
     * The tilt of the illuminated fraction of the moon in degrees clockwise from the top of the moon.
     */
    fun getMoonTilt(time: ZonedDateTime, location: Coordinate): Float {
        return moon.getTilt(time.toUniversalTime(), location)
    }

    /**
     * The parallactic angle of the moon in degrees.
     */
    fun getMoonParallacticAngle(time: ZonedDateTime, location: Coordinate): Float {
        return AstroUtils.getParallacticAngle(moon, time.toUniversalTime(), location)
    }
}
