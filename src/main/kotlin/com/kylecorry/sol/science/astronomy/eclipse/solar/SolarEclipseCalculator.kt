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

/**
 * Internal class responsible for calculating solar eclipses.
 *
 * @property precision Time interval for incrementing the search for eclipses, default is 1 minute.
 * @param maxDuration Maximum duration to search for a solar eclipse, default is 5 years.
 */
internal class SolarEclipseCalculator(
    private val precision: Duration = Duration.ofMinutes(1),
    maxDuration: Duration? = null
) : EclipseCalculator {

    private val sun = Sun()
    private val moon = Moon()

    private val _maxDuration = maxDuration ?: Duration.ofDays(365 * 5)
    private val _minEclipseDuration = Duration.ofMinutes(1)

    override fun getNextEclipse(after: Instant, location: Coordinate): Eclipse? {

        // Calculate the approximate time of the next eclipse
        val nextEclipseTime = getNextEclipseTime(after, location) ?: return null

        // Now that we have the approximate time, we can search for the exact time by incrementing the time by the precision
        // in both directions until we find the exact time of the start and end of the eclipse

        // Search parameters - 12 hours before and after the approximate time
        val maxSearch = Duration.ofHours(12)
        val minTime = nextEclipseTime.minus(maxSearch)
        val maxTime = nextEclipseTime.plus(maxSearch)

        // Initialize the peak magnitude and obscuration
        var maxMagnitude = 0f
        var maxObscuration = 0f
        var timeOfMaximum = nextEclipseTime

        // Search for the start time of the eclipse
        // This will either be when the sun and moon start to overlap or when the sun or moon is below the horizon
        var currentStartTime = nextEclipseTime.toUniversalTime()
        var start = nextEclipseTime
        while (start > minTime) {
            // Check if the sun or moon is below the horizon, if so then the eclipse is not visible anymore
            val sunCoordinates = getCoordinates(sun, currentStartTime, location)
            val moonCoordinates = getCoordinates(moon, currentStartTime, location)
            if (sunCoordinates.altitude < 0 || moonCoordinates.altitude < 0) {
                break
            }

            // Check the magnitude of the eclipse, if less than or equal to 0 then the eclipse is not visible anymore
            val magnitude = getMagnitude(currentStartTime, location, sunCoordinates, moonCoordinates)
            if (magnitude.first <= 0f) {
                break
            }

            // Record the maximum magnitude
            if (magnitude.first > maxMagnitude) {
                maxMagnitude = magnitude.first
                maxObscuration = magnitude.second
                timeOfMaximum = currentStartTime.toInstant()
            }

            // The eclipse was still active at this time, so move back in time
            start = currentStartTime.toInstant()
            currentStartTime = currentStartTime.minus(precision)
        }

        // Search for the end time of the eclipse
        // This will either be when the sun and moon cease to overlap or when the sun or moon is below the horizon
        var currentEndTime = nextEclipseTime.toUniversalTime()
        var end = nextEclipseTime
        while (end < maxTime) {
            // Check if the sun or moon is below the horizon, if so then the eclipse is not visible anymore
            val sunCoordinates = getCoordinates(sun, currentEndTime, location)
            val moonCoordinates = getCoordinates(moon, currentEndTime, location)
            if (sunCoordinates.altitude < 0 || moonCoordinates.altitude < 0) {
                break
            }

            // Check the magnitude of the eclipse, if it is 0 then the eclipse is not visible anymore
            val magnitude = getMagnitude(currentEndTime, location, sunCoordinates, moonCoordinates)
            if (magnitude.first == 0f) {
                break
            }

            // Record the maximum magnitude
            if (magnitude.first > maxMagnitude) {
                maxMagnitude = magnitude.first
                maxObscuration = magnitude.second
                timeOfMaximum = currentEndTime.toInstant()
            }

            // The eclipse was still active at this time, so move forward in time
            end = currentEndTime.toInstant()
            currentEndTime = currentEndTime.plus(precision)
        }

        // If the eclipse is too short, ignore it
        if (Duration.between(start, end) < _minEclipseDuration) {
            return null
        }

        return Eclipse(start, end, maxMagnitude, maxObscuration, timeOfMaximum)
    }

    /**
     * Get the approximate time of the next eclipse.
     * This will search for eclipses until the maximum duration is reached, at which point it will return null.
     *
     * @param after The time to start searching after.
     * @param location The location to search for eclipses.
     */
    private fun getNextEclipseTime(after: Instant, location: Coordinate): Instant? {
        // Start at the given time
        var timeFromStart = Duration.ZERO
        val startUT = after.toUniversalTime()

        // The default skip is 15 minutes
        // If the eclipse is less than 15 minutes, then it may be missed
        val defaultSkip = Duration.ofMinutes(15)

        // Search until the maximum duration is reached or until an eclipse is found
        while (timeFromStart < _maxDuration) {

            // Get the location of the sun and moon at the current time
            val currentTime = startUT.plus(timeFromStart)

            val sunCoordinates = getCoordinates(sun, currentTime, location)
            val moonCoordinates = getCoordinates(moon, currentTime, location)

            // Skip ahead if conditions are not right for an eclipse
            val nextSkip = getNextSkip(currentTime, location, sunCoordinates, moonCoordinates)
            if (nextSkip != null) {
                timeFromStart = timeFromStart.plus(nextSkip)
                continue
            }

            // The conditions are right for an eclipse. If there is an eclipse, the magnitude will be greater than 0.
            val magnitude = getMagnitude(currentTime, location, sunCoordinates, moonCoordinates).first
            if (magnitude > 0) {
                return currentTime.toInstant()
            }

            // An eclipse was not found, but no conditions where met that indicate an eclipse is not possible,
            // so skip ahead by a little to check again.
            timeFromStart = timeFromStart.plus(defaultSkip)
        }

        // No eclipse was found
        return null
    }

    private var lastNewMoonTime: UniversalTime? = null

    /**
     * Calculate how far to skip ahead in time to potentially find the next eclipse. If conditions are not right for an
     * eclipse, then this will return the amount of time to skip ahead to check again.
     *
     * If conditions are right for an eclipse, then this will return null.
     *
     * @param time The current time.
     * @param location The location to search for eclipses.
     * @param sunCoordinates The coordinates of the sun at the current time.
     * @param moonCoordinates The coordinates of the moon at the current time.
     * @return The amount of time to skip ahead to check again, or null if conditions are right for an eclipse.
     */
    private fun getNextSkip(
        time: UniversalTime,
        location: Coordinate,
        sunCoordinates: HorizonCoordinate,
        moonCoordinates: HorizonCoordinate
    ): Duration? {
        // Skip ahead by a number of days proportional to the phase of the moon from new moon
        // TODO: A future enhancement would be to calculate the time of the next new moon and skip to slightly before it

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

        println(daysUntilNewMoon)

        // It is not possible for an eclipse to occur if the moon is not close to a new moon, so skip ahead
        if (daysUntilNewMoon > 2) {
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

        // If the moon is not close to the sun, skip a bit (based on the distance between the sun and moon)
        val distance = sunCoordinates.angularDistanceTo(moonCoordinates)
        if (distance > 10) {
            return Duration.ofHours(2)
        } else if (distance > 2) {
            return Duration.ofMinutes(30)
        }

        // Conditions are right for an eclipse, so don't skip ahead
        return null
    }

    /**
     * Get the amount of time until the next sunrise. This includes refraction and parallax.
     *
     * @param time The time to start searching after.
     * @param location The location to search for sunrises.
     * @return The amount of time until the next sunrise, or null if there is no sunrise.
     */
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

    /**
     * Get the amount of time until the next moonrise. This includes refraction and parallax.
     *
     * @param time The time to start searching after.
     * @param location The location to search for moonrises.
     * @return The amount of time until the next moonrise, or null if there is no moonrise.
     */
    private fun timeUntilMoonrise(time: UniversalTime, location: Coordinate): Duration? {
        val nextMoonrise = Astronomy.getNextMoonrise(
            time.atZone(ZoneId.of("UTC")),
            location,
            withRefraction = true,
            withParallax = true
        ) ?: return null

        return Duration.between(time.toInstant(), nextMoonrise.toInstant())
    }

    /**
     * Get the coordinates of the given celestial body at the given time. This includes refraction and parallax.
     *
     * @param locator The celestial body to get the coordinates for.
     * @param time The time to get the coordinates at.
     * @param location The location to get the coordinates at.
     */
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
            locator.getDistance(time)!! // Supplying the distance for parallax
        ).withRefraction()
    }

    /**
     * Get the magnitude of the eclipse.
     *
     * @param time The time of the eclipse.
     * @param location The location of the eclipse.
     * @param sunCoordinates The coordinates of the sun at the time of the eclipse.
     * @param moonCoordinates The coordinates of the moon at the time of the eclipse.
     * @return The magnitude and obscuration of the eclipse.
     */
    private fun getMagnitude(
        time: UniversalTime,
        location: Coordinate,
        sunCoordinates: HorizonCoordinate,
        moonCoordinates: HorizonCoordinate
    ): Pair<Float, Float> {
        // Calculate the angular distance between the sun and moon
        val angularDistance = sunCoordinates.angularDistanceTo(moonCoordinates)

        // Calculate the size of the sun and moon disks
        val moonRadius = moon.getAngularDiameter(time, location) / 2.0
        val sunRadius = sun.getAngularDiameter(time) / 2.0

        // Calculate the magnitude of the eclipse
        return getMagnitude(angularDistance, moonRadius, sunRadius)
    }

    /**
     * Calculate the magnitude of the eclipse. This is based on the formulas provided in https://doi.org/10.11578/dc.20190909.1
     *
     * @param angularDistance The angular distance between the sun and moon.
     * @param moonRadius The radius of the moon disk.
     * @param sunRadius The radius of the sun disk.
     * @return The magnitude and obscuration of the eclipse.
     */
    private fun getMagnitude(
        angularDistance: Double,
        moonRadius: Double,
        sunRadius: Double
    ): Pair<Float, Float> {
        // There is no eclipse, so return 0
        if (!isAnyEclipse(angularDistance, moonRadius, sunRadius)) {
            return 0f to 0f
        }

        // There is a total eclipse
        if (isTotalEclipse(angularDistance, moonRadius, sunRadius)) {
            // Calculate the ratio of the moon's diameter to the sun's diameter
            val diameterRatio = (moonRadius / sunRadius).toFloat()

            return if (sunRadius <= moonRadius) {
                // The sun is smaller than the moon, so the obscuration is 1 (sun is completely obscured)
                diameterRatio to 1f
            } else {
                // The sun is larger than the moon, and the moon is completely contained in the sun disk (sun is partially obscured)
                val sunArea = PI * sunRadius * sunRadius
                val moonArea = PI * moonRadius * moonRadius
                diameterRatio to (moonArea / sunArea).toFloat()
            }
        }

        // There is a partial eclipse, the obscured area can be calculated using sectors
        val distance2 = square(angularDistance)
        val moonRadius2 = square(moonRadius)
        val sunRadius2 = square(sunRadius)

        val s = (distance2 + sunRadius2 - moonRadius2) / (2 * angularDistance)
        val m = (distance2 + moonRadius2 - sunRadius2) / (2 * angularDistance)

        val h =
            sqrt(4 * distance2 * sunRadius2 - square(distance2 + sunRadius2 - moonRadius2)) / (2 * angularDistance)

        val triangleSun = h * s
        val triangleMoon = h * m

        // Calculate the areas of the sectors
        val sectorSun = sunRadius2 * acos(s / sunRadius)
        val sectorMoon = moonRadius2 * acos(m / moonRadius)

        // Subtract the areas of the triangles that form the sectors - the result is the segment areas
        val areaSun = sectorSun - triangleSun
        val areaMoon = sectorMoon - triangleMoon

        // Sum up both segments
        val totalArea = (areaSun + areaMoon).toFloat()

        // Calculate the obscuration of the sun (obscured area / sun area)
        val obscuration = totalArea / (PI * sunRadius2).toFloat()

        // Calculate the magnitude based on the length of the overlap between the sun and moon disks
        val lengthOverlap = (sunRadius + moonRadius) - abs(s + m)
        val magnitude = (lengthOverlap / (2 * sunRadius)).toFloat()

        return magnitude to obscuration
    }

    /**
     * Check if there is any eclipse. This is the case if the angular distance between the sun and moon is smaller than the sum of their radii.
     *
     * @param angularDistance The angular distance between the sun and moon.
     * @param moonRadius The radius of the moon disk (degrees).
     * @param sunRadius The radius of the sun disk (degrees).
     * @return True if there is any eclipse, false otherwise.
     */
    private fun isAnyEclipse(
        angularDistance: Double,
        moonRadius: Double,
        sunRadius: Double
    ): Boolean {
        return angularDistance <= moonRadius + sunRadius
    }

    /**
     * Check if there is a total eclipse. This is the case if the angular distance between the sun and moon is smaller than the difference of their radii.
     *
     * @param angularDistance The angular distance between the sun and moon.
     * @param moonRadius The radius of the moon disk (degrees).
     * @param sunRadius The radius of the sun disk (degrees).
     * @return True if there is a total eclipse, false otherwise.
     */
    private fun isTotalEclipse(
        angularDistance: Double,
        moonRadius: Double,
        sunRadius: Double
    ): Boolean {
        return angularDistance <= abs(moonRadius - sunRadius)
    }

}