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
import com.kylecorry.sol.science.astronomy.units.*
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
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
    private val provider = SolarEclipseParameterProvider()

    private val shouldLog = false

    private val _maxDuration = maxDuration ?: Duration.ofDays(365 * 5)
    private val _minEclipseDuration = Duration.ofMinutes(1)

    private val _log = mutableListOf<Pair<UniversalTime, String>>()

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

        if (shouldLog) {
            _log.clear()
        }

        timeFromStart = timeFromStart.minusDays(10)
        while (timeFromStart < _maxDuration) {
            val currentTime = startUT.plus(timeFromStart)
            val instant = currentTime.toInstant()

            // Get the next time of a solar eclipse
            val nextEclipse = provider.getNextSolarEclipseParameters(instant)

            // TODO: Check to see if the eclipse will even be visible on earth

            // Search around the maximum time of the eclipse to see if it is visible
            val searchAmount = Duration.ofHours(4)
            val minimum = nextEclipse.maximum.minus(searchAmount).coerceAtLeast(after)
            val maximum = nextEclipse.maximum.plus(searchAmount).coerceAtLeast(after)
            val start = nextEclipse.maximum.coerceAtLeast(after)

            val t = spreadSearch(minimum, maximum, start, defaultSkip) {
                if (shouldLog) {
                    _log.add(currentTime to "Eclipse check")
                }
                val ut = it.toUniversalTime()

                // Verify that the sun is up
                val sunCoordinates = getCoordinates(sun, ut, location)
                if (sunCoordinates.altitude < 0) {
                    return@spreadSearch false
                }

                // Verify that the moon is up
                val moonCoordinates = getCoordinates(moon, ut, location)
                if (moonCoordinates.altitude < 0) {
                    return@spreadSearch false
                }

                // Verify that an eclipse is happening
                val magnitude = getMagnitude(ut, location, sunCoordinates, moonCoordinates)
                magnitude.first > 0
            }

            if (t != null) {
                if (shouldLog) {
                    _log.add(currentTime to "Found eclipse")
                    println(_log.size)
                    println(_log.joinToString("\n") { "${it.first},${it.second}" })
                }
                return t
            }

            // Skip 10 days and try again
            timeFromStart = timeFromStart.plus(Duration.between(instant, nextEclipse.maximum).plusDays(10))
        }

        if (shouldLog) {
            println(_log.size)
            println(_log.joinToString("\n") { "${it.first},${it.second}" })
        }

        return null
    }

    private fun spreadSearch(
        minimum: Instant,
        maximum: Instant,
        start: Instant,
        interval: Duration,
        test: (time: Instant) -> Boolean
    ): Instant? {
        var left = start
        var right = start

        // Search each side of the start time until the interval is reached
        while (left >= minimum || right <= maximum) {
            if (left >= minimum && test(left)) {
                return left
            }
            if (right <= maximum && test(right)) {
                return right
            }
            left = left.minus(interval)
            right = right.plus(interval)
        }

        return null
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