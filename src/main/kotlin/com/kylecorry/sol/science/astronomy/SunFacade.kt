package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.trigonometry.Trigonometry.deltaAngle
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.sun.SolarRadiationCalculator
import com.kylecorry.sol.science.astronomy.units.EclipticCoordinate
import com.kylecorry.sol.science.astronomy.units.fromJulianDay
import com.kylecorry.sol.science.astronomy.units.toJulianDay
import com.kylecorry.sol.science.astronomy.units.toLocal
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.science.astronomy.rst.RobustRiseSetTransitTimeCalculator
import com.kylecorry.sol.time.Time.atEndOfDay
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

internal object SunFacade {
    private const val MAX_SOLAR_LONGITUDE_REFINEMENT_ITERATIONS = 100

    private val sun = Sun()
    private val radiation = SolarRadiationCalculator()
    private val riseSetTransitCalculator = RobustRiseSetTransitTimeCalculator()

    fun getSunEvents(
        date: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): RiseSetTransitTimes {

        val altitude = when (mode) {
            SunTimesMode.Actual -> -0.8333
            SunTimesMode.Civil -> -6.0
            SunTimesMode.Nautical -> -12.0
            SunTimesMode.Astronomical -> -18.0
        }

        return riseSetTransitCalculator.calculate(
            sun,
            date,
            location,
            altitude,
            withRefraction,
            withParallax
        )
    }

    fun getSunAltitude(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Float {
        return AstroUtils.getAltitude(
            sun,
            time.toUniversalTime(),
            location,
            withRefraction,
            withParallax
        )
    }

    fun getSunAzimuth(
        time: ZonedDateTime,
        location: Coordinate,
        withParallax: Boolean = false
    ): Bearing {
        return AstroUtils.getAzimuth(sun, time.toUniversalTime(), location, withParallax)
    }

    fun getNextSunset(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction, withParallax)
        if (today.set != null && today.set > time) {
            return today.set
        }

        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction, withParallax)
        if (tomorrow.set != null && tomorrow.set > time) {
            return tomorrow.set
        }

        return null
    }

    fun getNextSunrise(
        time: ZonedDateTime,
        location: Coordinate,
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): ZonedDateTime? {
        val today = getSunEvents(time, location, mode, withRefraction, withParallax)
        if (today.rise != null && today.rise > time) {
            return today.rise
        }

        val tomorrow = getSunEvents(time.plusDays(1), location, mode, withRefraction, withParallax)
        if (tomorrow.rise != null && tomorrow.rise > time) {
            return tomorrow.rise
        }

        return null
    }

    fun isSunUp(
        time: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Boolean {
        return getSunAltitude(time, location, withRefraction, withParallax) > 0
    }

    fun getDaylightLength(
        date: ZonedDateTime,
        location: Coordinate,
        sunTimesMode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Duration {
        val startOfDay = date.atStartOfDay()
        val sunrise =
            getNextSunrise(startOfDay, location, sunTimesMode, withRefraction, withParallax)
        val sunset = getNextSunset(startOfDay, location, sunTimesMode, withRefraction, withParallax)

        if (sunrise != null && sunset != null && sunset > sunrise) {
            // Rise in morning, set at night
            return Duration.between(sunrise, sunset)
        } else if (sunrise == null && sunset == null) {
            // Sun doesn't rise or set
            return if (isSunUp(
                    startOfDay,
                    location,
                    withRefraction,
                    withParallax
                )
            ) Duration.between(
                startOfDay,
                startOfDay.plusDays(1)
            ) else Duration.ZERO
        } else if (sunrise != null && sunset == null) {
            // Sun rises but doesn't set
            return Duration.between(sunrise, startOfDay.plusDays(1))
        } else if (sunrise == null) {
            // Sun sets but doesn't rise
            return Duration.between(startOfDay, sunset)
        } else {
            // Sun sets in morning, rises at night
            return Duration.between(startOfDay, sunset)
                .plus(Duration.between(sunrise, startOfDay.plusDays(1)))
        }
    }

    fun getSunDistance(time: ZonedDateTime): Distance {
        return sun.getDistance(time.toUniversalTime())
    }

    /**
     * Gets the solar radiation for the given time in kW/m^2
     */
    fun getSolarRadiation(
        date: ZonedDateTime,
        location: Coordinate,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Double {
        return radiation.getRadiation(
            date.toUniversalTime(),
            location,
            withRefraction = withRefraction,
            withParallax = withParallax
        )
    }

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
    ): Double {
        return radiation.getRadiation(
            date.toUniversalTime(),
            location,
            tilt,
            azimuth,
            withRefraction,
            withParallax
        )
    }

    /**
     * Gets the times the sun is above the horizon within approximately a day.
     * If the sun does not set, it will return from the last rise (or start of day) until the end of the day.
     * @param location The location of the observer
     * @param time The current time
     * @param nextRiseOffset The duration before the next rise to switch to the next day's times
     * @param mode The mode to use for calculating sun times
     * @param withRefraction True to correct for atmospheric refraction
     * @param withParallax True to correct for parallax
     * @return The range of times the sun is above the horizon or null if it is not above the horizon
     * within approximately a day.
     */
    fun getSunAboveHorizonTimes(
        location: Coordinate,
        time: ZonedDateTime,
        nextRiseOffset: Duration = Duration.ofHours(6),
        mode: SunTimesMode = SunTimesMode.Actual,
        withRefraction: Boolean = false,
        withParallax: Boolean = false
    ): Range<ZonedDateTime>? {
        return AboveHorizonFacade.getAboveHorizonTimes(
            location,
            time,
            nextRiseOffset,
            { loc, t -> isSunUp(t, loc, withRefraction, withParallax) },
            { loc, t -> getSunEvents(t, loc, mode, withRefraction, withParallax) }
        )
    }

    fun getClosestNight(
        time: ZonedDateTime?,
        location: Coordinate,
        sunTimesMode: SunTimesMode
    ): Range<ZonedDateTime>? {
        if (time == null) {
            return null
        }
        val yesterday = getSunEvents(time.minusDays(1), location, sunTimesMode)
        val today = getSunEvents(time, location, sunTimesMode)
        val tomorrow = getSunEvents(time.plusDays(1), location, sunTimesMode)

        val didNotSet = yesterday.set == null || today.set == null
        val willNotRise = today.rise == null || tomorrow.rise == null

        if (didNotSet || willNotRise) {
            return if (!isSunUp(time, location) && today.rise == null) {
                // Sun does not set
                Range(time.atStartOfDay(), time.atEndOfDay())
            } else {
                null
            }
        }

        val lastNight = Range(yesterday.set, today.rise)
        val tonight = Range(today.set, tomorrow.rise)

        val timeUntilLastNight = Duration.between(time, lastNight.end).abs()
        val timeUntilTonight = Duration.between(time, tonight.start).abs()


        return if (timeUntilLastNight < timeUntilTonight) {
            lastNight
        } else {
            tonight
        }

    }

    fun getNextTimeAtSolarLongitude(longitude: Float, today: ZonedDateTime): ZonedDateTime {
        val threshold = 1f
        var d = today
        for (i in 0..365) {
            val date = today.plusDays(i.toLong())
            val sl = getSolarLongitude(date)
            if (deltaAngle(longitude, sl).absoluteValue < threshold) {
                d = date
                break
            }
        }

        var jd = d.toUniversalTime().toJulianDay()
        var correction: Double
        var iterations = 0

        do {
            val ut = fromJulianDay(jd)
            val coords = sun.getCoordinates(ut)
            val solarLon = EclipticCoordinate.fromEquatorial(
                coords,
                ut
            ).eclipticLongitude
            correction = 58 * sinDegrees(longitude - solarLon)
            jd += correction
            iterations++
        } while (correction.absoluteValue > 0.00001 && iterations < MAX_SOLAR_LONGITUDE_REFINEMENT_ITERATIONS)

        check(correction.absoluteValue <= 0.00001) {
            "Solar longitude refinement did not converge within $MAX_SOLAR_LONGITUDE_REFINEMENT_ITERATIONS iterations"
        }

        return fromJulianDay(jd).toLocal(today.zone)
    }

    fun getSolarLongitude(date: ZonedDateTime): Float {
        val coords = sun.getCoordinates(date.toUniversalTime())
        return EclipticCoordinate.fromEquatorial(
            coords,
            date.toUniversalTime()
        ).eclipticLongitude.toFloat()
    }
}
