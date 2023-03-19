package com.kylecorry.sol.science.astronomy.eclipse.solar

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.SunTimesMode
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseCalculator
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toInstant
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

class SolarEclipseCalculator(
    private val precision: Duration = Duration.ofMinutes(1),
    maxDuration: Duration? = null
) : EclipseCalculator {

    private val sun = Sun()
    private val moon = Moon()

    private val _maxDuration = maxDuration ?: Duration.ofDays(365 * 5)

    override fun getNextEclipse(after: Instant, location: Coordinate): Eclipse? {
        val nextEclipseTime = getNextEclipseTime(after, location) ?: return null

        // Search parameters
        val maxSearch = Duration.ofHours(24)
        val minTime = nextEclipseTime.minus(maxSearch)
        val maxTime = nextEclipseTime.plus(maxSearch)

        var maxMagnitude = 0f
        var timeOfMaximum = nextEclipseTime

        // Search for the start of the eclipse
        var currentStartTime = nextEclipseTime.toUniversalTime()

        var start = nextEclipseTime
        while (start > minTime) {
            val sunCoordinates = getCoordinates(sun, currentStartTime, location)
            val moonCoordinates = getCoordinates(moon, currentStartTime, location)

            // Sun or moon is below the horizon
            if (sunCoordinates.altitude < 0 || moonCoordinates.altitude < 0) {
                break
            }

            val magnitude = getMagnitude(currentStartTime, location, sunCoordinates, moonCoordinates)

            // Eclipse was not found
            if (magnitude == 0f) {
                break
            }

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                timeOfMaximum = currentStartTime.toInstant()
            }

            start = currentStartTime.toInstant()
            currentStartTime = currentStartTime.minus(precision)
        }

        // Search for the end of the eclipse
        var currentEndTime = nextEclipseTime.toUniversalTime()
        var end = nextEclipseTime

        while (end < maxTime) {
            val sunCoordinates = getCoordinates(sun, currentEndTime, location)
            val moonCoordinates = getCoordinates(moon, currentEndTime, location)

            // Sun or moon is below the horizon
            if (sunCoordinates.altitude < 0 || moonCoordinates.altitude < 0) {
                break
            }

            val magnitude = getMagnitude(currentEndTime, location, sunCoordinates, moonCoordinates)

            // Eclipse was not found
            if (magnitude == 0f) {
                break
            }

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                timeOfMaximum = currentEndTime.toInstant()
            }

            end = currentEndTime.toInstant()
            currentEndTime = currentEndTime.plus(precision)
        }

        return Eclipse(start, end, maxMagnitude, timeOfMaximum)
    }

    private fun getNextEclipseTime(after: Instant, location: Coordinate): Instant? {
        var timeFromStart = Duration.ZERO
        val startUT = after.toUniversalTime()

        val defaultSkip = Duration.ofMinutes(15)

        while (timeFromStart < _maxDuration) {
            val currentTime = startUT.plus(timeFromStart)

            val sunCoordinates = getCoordinates(sun, currentTime, location)
            val moonCoordinates = getCoordinates(moon, currentTime, location)

            // Skip ahead if conditions are not right for an eclipse
            val nextSkip = getNextSkip(currentTime, location, sunCoordinates, moonCoordinates)
            if (nextSkip != null) {
                timeFromStart = timeFromStart.plus(nextSkip)
                continue
            }

            val magnitude = getMagnitude(currentTime, location, sunCoordinates, moonCoordinates)
            if (magnitude > 0) {
                return currentTime.toInstant()
            }

            timeFromStart = timeFromStart.plus(defaultSkip)
        }

        return null
    }

    private fun getNextSkip(
        time: UniversalTime,
        location: Coordinate,
        sunCoordinates: HorizonCoordinate,
        moonCoordinates: HorizonCoordinate
    ): Duration? {
        // If the moon is close to full, skip a bit
        val phase = moon.getPhase(time)

        val daysUntilNewMoon = when (phase.phase) {
            MoonTruePhase.ThirdQuarter -> 2
            MoonTruePhase.WaningGibbous -> 4
            MoonTruePhase.Full -> 8
            MoonTruePhase.WaxingGibbous -> 12
            MoonTruePhase.FirstQuarter -> 14
            MoonTruePhase.WaxingCrescent -> 22
            else -> 0
        }

        if (daysUntilNewMoon > 0) {
            return Duration.ofDays(daysUntilNewMoon.toLong())
        }

        // If the sun is down, skip to the next sunrise
        if (sunCoordinates.altitude < 0) {
            return timeUntilSunrise(time, location)?.plusMinutes(15)
        }

        // If the moon is down, skip to the next moonrise
        if (moonCoordinates.altitude < 0) {
            return timeUntilMoonrise(time, location)?.plusMinutes(15)
        }

        // If the moon is not close to the sun, skip a bit
        val distance = sunCoordinates.angularDistanceTo(moonCoordinates)
        if (distance > 10) {
            return Duration.ofHours(2)
        } else if (distance > 2) {
            return Duration.ofMinutes(30)
        }

        return null
    }

    private fun getMagnitude(
        time: UniversalTime,
        location: Coordinate,
        sunCoordinates: HorizonCoordinate,
        moonCoordinates: HorizonCoordinate
    ): Float {
        val angularDistance = sunCoordinates.angularDistanceTo(moonCoordinates)
        val moonRadius = moon.getAngularDiameter(time, location) / 2.0
        val sunRadius = sun.getAngularDiameter(time) / 2.0

        return getMagnitude(angularDistance, moonRadius, sunRadius)
    }

    private fun timeUntilSunrise(time: UniversalTime, location: Coordinate): Duration? {
        val nextSunrise = Astronomy.getNextSunrise(
            time.atZone(ZoneId.of("UTC")),
            location,
            SunTimesMode.Actual,
            withRefraction = true,
            withParallax = true
        ) ?: return null

        return Duration.between(time.toInstant(), nextSunrise.toInstant())
    }

    private fun timeUntilMoonrise(time: UniversalTime, location: Coordinate): Duration? {
        val nextMoonrise = Astronomy.getNextMoonrise(
            time.atZone(ZoneId.of("UTC")),
            location,
            withRefraction = true,
            withParallax = true
        ) ?: return null

        return Duration.between(time.toInstant(), nextMoonrise.toInstant())
    }

    private fun getCoordinates(
        locator: ICelestialLocator,
        time: UniversalTime,
        location: Coordinate
    ): HorizonCoordinate {
        val coordinates = locator.getCoordinates(time)
        return HorizonCoordinate.fromEquatorial(
            coordinates,
            time,
            location,
            locator.getDistance(time)!!
        ).withRefraction()
    }

    private fun getMagnitude(
        angularDistance: Double,
        moonRadius: Double,
        sunRadius: Double
    ): Float {
        if (!isAnyEclipse(angularDistance, moonRadius, sunRadius)) {
            return 0f
        }

        if (isTotalEclipse(angularDistance, moonRadius, sunRadius)) {
            return if (sunRadius <= moonRadius) {
                1f
            } else {
                val sunArea = PI * sunRadius * sunRadius
                val moonArea = PI * moonRadius * moonRadius
                (moonArea / sunArea).toFloat()
            }
        }

        val distance2 = square(angularDistance)
        val moonRadius2 = square(moonRadius)
        val sunRadius2 = square(sunRadius)

        val s = (distance2 + sunRadius2 - moonRadius2) / (2 * angularDistance)
        val m = (distance2 + moonRadius2 - sunRadius2) / (2 * angularDistance)

        val h =
            sqrt(4 * distance2 * sunRadius2 - square(distance2 + sunRadius2 - moonRadius2)) / (2 * angularDistance)

        val triangleSun = h * s
        val triangleMoon = h * m

        val sectorSun = sunRadius2 * acos(s / sunRadius)
        val sectorMoon = moonRadius2 * acos(m / moonRadius)

        val areaSun = sectorSun - triangleSun
        val areaMoon = sectorMoon - triangleMoon

        val totalArea = (areaSun + areaMoon).toFloat()

        return totalArea / (PI * sunRadius2).toFloat()
    }

    private fun isAnyEclipse(
        angularDistance: Double,
        moonRadius: Double,
        sunRadius: Double
    ): Boolean {
        return angularDistance <= moonRadius + sunRadius
    }

    private fun isTotalEclipse(
        angularDistance: Double,
        moonRadius: Double,
        sunRadius: Double
    ): Boolean {
        return angularDistance <= abs(moonRadius - sunRadius)
    }

}