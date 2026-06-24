package com.kylecorry.sol.science.astronomy.eclipse.lunar

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.geometry.Circle
import com.kylecorry.sol.math.geometry.Geometry
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseCalculator
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.math.absoluteValue

internal abstract class AbstractUmbralLunarEclipseCalculator : EclipseCalculator {
    override fun getNextEclipse(after: Instant, location: Coordinate): Eclipse? {
        return getNextEclipseHelper(after.minus(Duration.ofDays(20)), after, location, 100)
    }

    protected abstract fun getMagnitudeThreshold(): Double
    protected abstract fun getSemiDuration(parameters: LunarEclipseParameters): Duration

    private fun getNextEclipseHelper(
        after: Instant,
        atLeastInstant: Instant,
        location: Coordinate,
        iterationsRemaining: Int
    ): Eclipse? {
        if (iterationsRemaining == 0) {
            return null
        }

        val provider = LunarEclipseParameterProvider()
        val parameters = provider.getNextLunarEclipseParameters(after)

        val moonDiameterInEarthRadii = Moon.DIAMETER_IN_EARTH_RADII
        val umbraRadius = LUNAR_ECLIPSE_UMBRA_RADIUS - parameters.umbralConeRadius

        val umbralDiff = LUNAR_ECLIPSE_UMBRA_OUTER_CONTACT_RADIUS -
            parameters.umbralConeRadius -
            parameters.minDistanceFromCenter.absoluteValue
        val magnitude = umbralDiff / moonDiameterInEarthRadii

        if (magnitude < getMagnitudeThreshold()) {
            return getNextEclipseHelper(
                parameters.maximum.plus(Duration.ofDays(10)),
                atLeastInstant,
                location,
                iterationsRemaining - 1
            )
        }

        // It is a total eclipse
        val semiDuration = getSemiDuration(parameters)

        val time = Range(
            parameters.maximum - semiDuration,
            parameters.maximum + semiDuration,
        )

        val isAfterInstant = time.end.isAfter(atLeastInstant)
        if (!isAfterInstant) {
            return getNextEclipseHelper(
                parameters.maximum.plus(Duration.ofDays(10)),
                atLeastInstant,
                location,
                iterationsRemaining - 1
            )
        }

        val upAtStart = Astronomy.isMoonUp(time.start.atZone(ZoneId.of("UTC")), location)
        val upAtEnd = Astronomy.isMoonUp(time.end.atZone(ZoneId.of("UTC")), location)

        if (upAtStart || upAtEnd) {
            val circle1 = Circle(Vector2.zero, Moon.RADIUS_IN_EARTH_RADII.toFloat())
            val circle2 = Circle(Vector2(parameters.minDistanceFromCenter.toFloat(), 0f), umbraRadius.toFloat())
            val obscuration = Geometry.getIntersectionArea(circle2, circle1) / circle1.area()

            var start = time.start
            var end = time.end

            if (!upAtStart) {
                // Find the moon rise time
                val moonRise = Astronomy.getNextMoonrise(
                    time.start.toZonedDateTime(),
                    location,
                    withRefraction = true,
                    withParallax = true
                )
                start = moonRise?.toInstant() ?: start
            }

            if (!upAtEnd) {
                // Find moon set time
                val moonSet = Astronomy.getNextMoonset(
                    time.start.toZonedDateTime(),
                    location,
                    withRefraction = true,
                    withParallax = true
                )
                end = moonSet?.toInstant() ?: end
            }

            val maximum = time.start.plus(Duration.between(time.start, time.end).dividedBy(2)).coerceIn(start, end)
            return Eclipse(start, end, magnitude.toFloat(), obscuration, maximum)
        }

        return getNextEclipseHelper(
            parameters.maximum.plus(Duration.ofDays(10)),
            atLeastInstant,
            location,
            iterationsRemaining - 1
        )
    }

    override fun getMagnitude(time: Instant, location: Coordinate): Float? {
        return null
    }

    override fun getObscuration(time: Instant, location: Coordinate): Float? {
        return null
    }

}
