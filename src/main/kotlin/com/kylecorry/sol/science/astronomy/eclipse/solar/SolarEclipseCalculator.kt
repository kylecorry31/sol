package com.kylecorry.sol.science.astronomy.eclipse.solar

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.science.astronomy.AstroSearch
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseCalculator
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.Instant
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

    override fun getNextEclipse(after: Instant, location: Coordinate): Eclipse? {
        // Calculate the approximate time of the next eclipse
        val nextEclipseTime = getNextEclipseTime(after, location) ?: return null

        // Now that we have the approximate time, we can search for the exact time

        val maxSearch = Duration.ofHours(6)
        val minTime = nextEclipseTime.minus(maxSearch)
        val maxTime = nextEclipseTime.plus(maxSearch)

        // Search for the start time of the eclipse
        val start = AstroSearch.findStart(Range(minTime, nextEclipseTime), precision) { time ->
            val ut = time.toUniversalTime()
            if (shouldLog) {
                println("$ut, Searching for start time")
            }

            getVisibleMagnitude(ut, location).first > 0
        } ?: return null

        val end = AstroSearch.findEnd(Range(nextEclipseTime, maxTime), precision) { time ->
            val ut = time.toUniversalTime()
            if (shouldLog) {
                println("$ut, Searching for end time")
            }

            getVisibleMagnitude(ut, location).first > 0
        } ?: return null

        val peak = AstroSearch.findPeak(Range(start, end), precision) { time ->
            val ut = time.toUniversalTime()
            if (shouldLog) {
                println("$ut, Searching for peak time")
            }

            getVisibleMagnitude(ut, location).first
        }

        val peakUt = peak.toUniversalTime()
        val magnitude = getVisibleMagnitude(peakUt, location)
        val maxMagnitude = magnitude.first
        val maxObscuration = magnitude.second

        // If the eclipse is too short, ignore it
        if (Duration.between(start, end) < _minEclipseDuration) {
            return null
        }

        return Eclipse(start, end, maxMagnitude, maxObscuration, peak)
    }

    override fun getMagnitude(time: Instant, location: Coordinate): Float {
        return getVisibleMagnitude(time.toUniversalTime(), location).first
    }

    override fun getObscuration(time: Instant, location: Coordinate): Float {
        return getVisibleMagnitude(time.toUniversalTime(), location).second
    }

    /**
     * Get the approximate time of the next eclipse.
     * This will search for eclipses until the maximum duration is reached, at which point it will return null.
     *
     * @param after The time to start searching after.
     * @param location The location to search for eclipses.
     */
    private fun getNextEclipseTime(after: Instant, location: Coordinate): Instant? {
        val maxSearch = after.plus(_maxDuration)

        // Start at the given time
        var timeFromStart = Duration.ZERO

        // If the eclipse is less than 15 minutes, then it may be missed
        val precision = Duration.ofMinutes(15)

        // The formula to get the next eclipse does not work well when too close to an eclipse,
        // so start 10 days prior, and filter results to be only be after the given time
        timeFromStart = timeFromStart.minusDays(10)
        while (timeFromStart < _maxDuration) {
            val instant = after.plus(timeFromStart)

            // Get the next time of a solar eclipse
            val nextEclipse = provider.getNextSolarEclipseParameters(instant)

            // TODO: Check to see if the eclipse will even be visible on earth

            // Search around the maximum time of the eclipse to see if it is visible
            val searchAmount = Duration.ofHours(3)
            val minimum = nextEclipse.maximum.minus(searchAmount).coerceIn(after, maxSearch)
            val maximum = nextEclipse.maximum.plus(searchAmount).coerceIn(after, maxSearch)
            val start = nextEclipse.maximum.coerceIn(after, maxSearch)

            if (minimum >= maxSearch) {
                return null
            }

            val visibleTime = AstroSearch.findEvent(Range(minimum, maximum), precision, start) {
                val ut = it.toUniversalTime()

                if (shouldLog) {
                    println("$ut, Eclipse check")
                }

                getVisibleMagnitude(ut, location).first > 0
            }

            if (visibleTime != null) {
                return visibleTime
            }

            // Skip 10 days and try again
            timeFromStart += Duration.between(instant, nextEclipse.maximum).plusDays(10)
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
     * Get the magnitude of the eclipse. This will also return 0 if the sun or moon is below the horizon.
     *
     * @param time The time of the eclipse.
     * @param location The location of the eclipse.
     * @return The magnitude and obscuration of the eclipse.
     */
    private fun getVisibleMagnitude(
        time: UniversalTime,
        location: Coordinate
    ): Pair<Float, Float> {

        val sunCoordinates = getCoordinates(sun, time, location)
        if (sunCoordinates.altitude <= 0) {
            return 0f to 0f
        }

        val moonCoordinates = getCoordinates(moon, time, location)
        if (moonCoordinates.altitude <= 0) {
            return 0f to 0f
        }

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