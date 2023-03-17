package com.kylecorry.sol.science.astronomy.eclipse.solar

import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.SunTimesMode
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseCalculator
import com.kylecorry.sol.science.astronomy.locators.ICelestialLocator
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.locators.Sun
import com.kylecorry.sol.science.astronomy.units.HorizonCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toInstant
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

class SolarEclipseCalculator(
    private val precision: Duration = Duration.ofMinutes(1),
    private val maxDuration: Duration = Duration.ofDays(365 * 5),
    private val totalOnly: Boolean = false
) :
    EclipseCalculator {

    private val sun = Sun()
    private val moon = Moon()

    override fun getNextEclipse(after: Instant, location: Coordinate): Eclipse? {
        var timeFromStart = Duration.ZERO

        val startUT = after.toUniversalTime()

        var start: Instant? = null
        var end: Instant? = null
        var maxMagnitude = 0f
        var minDistance = 360.0

        while (timeFromStart < maxDuration) {
            val currentTime = startUT.plus(timeFromStart)

            // Skip check if the moon is close to full
            if (moon.getPhase(currentTime).illumination > 0.5f) {
                timeFromStart = timeFromStart.plus(Duration.ofHours(6))
                continue
            }

            val sunLocation =
                getCoordinates(sun, currentTime, location)

            // If the sun is below the horizon, skip to the next sunrise
            if (sunLocation.altitude < 0) {
                if (start != null) {
                    end = currentTime.toInstant()
                    break
                }
                timeFromStart =
                    timeFromStart.plus(timeUntilSunrise(currentTime, location) ?: precision)
                continue
            }

            val moonLocation = getCoordinates(moon, currentTime, location)

            // If the moon is below the horizon, skip to the next moonrise
            if (moonLocation.altitude < 0) {
                if (start != null) {
                    end = currentTime.toInstant()
                    break
                }

                timeFromStart =
                    timeFromStart.plus(timeUntilMoonrise(currentTime, location) ?: precision)
                continue
            }

            val angularDistance = sunLocation.angularDistanceTo(moonLocation)
            if (angularDistance < minDistance) {
                minDistance = angularDistance
            }

            // Not close enough to even consider
            if (angularDistance > 1) {
                if (start != null) {
                    end = currentTime.toInstant()
                    break
                }
                timeFromStart = timeFromStart.plus(precision)
                continue
            }

            val moonRadius = moon.getAngularDiameter(currentTime, location) / 2.0
            val sunRadius = sun.getAngularDiameter(currentTime) / 2.0

            if (isEclipse(angularDistance, moonRadius, sunRadius)) {
                if (start == null) {
                    start = currentTime.toInstant()
                }
                val magnitude = getMagnitude(angularDistance, moonRadius, sunRadius)
                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude
                }
            } else {
                if (start != null) {
                    end = currentTime.toInstant()
                    break
                }
            }

            timeFromStart = timeFromStart.plus(precision)
        }

        if (start == null || end == null) {
            return null
        }

        return Eclipse(start, end, maxMagnitude)
    }

    private fun timeUntilSunrise(time: UniversalTime, location: Coordinate): Duration? {
        val nextSunrise = Astronomy.getNextSunrise(
            time.atZone(ZoneId.of("UTC")),
            location,
            SunTimesMode.Actual,
            withRefraction = true,
            withParallax = true
        ) ?: return null

        return Duration.between(time.toInstant(), nextSunrise.toInstant()).plusMinutes(15)
    }

    private fun timeUntilMoonrise(time: UniversalTime, location: Coordinate): Duration? {
        val nextMoonrise = Astronomy.getNextMoonrise(
            time.atZone(ZoneId.of("UTC")),
            location,
            withRefraction = true,
            withParallax = true
        ) ?: return null

        return Duration.between(time.toInstant(), nextMoonrise.toInstant()).plusMinutes(15)
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

    private fun isEclipse(angularDistance: Double, moonRadius: Double, sunRadius: Double): Boolean {
        return when (totalOnly) {
            true -> isTotalEclipse(angularDistance, moonRadius, sunRadius)
            false -> isAnyEclipse(angularDistance, moonRadius, sunRadius)
        }
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