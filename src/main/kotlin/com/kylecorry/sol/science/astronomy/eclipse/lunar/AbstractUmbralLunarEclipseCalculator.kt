package com.kylecorry.sol.science.astronomy.eclipse.lunar

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.geometry.Circle
import com.kylecorry.sol.math.geometry.Geometry
import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.eclipse.Eclipse
import com.kylecorry.sol.science.astronomy.eclipse.EclipseCalculator
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

        val moonDiameterInEarthRadii = 0.545
        val moonRadiusInEarthRadii = moonDiameterInEarthRadii / 2
        val umbraRadius = 0.7403 - parameters.umbralConeRadius

        val magnitude =
            (1.0128 - parameters.umbralConeRadius - parameters.minDistanceFromCenter.absoluteValue) / moonDiameterInEarthRadii

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
            val circle1 = Circle(Vector2.zero, moonRadiusInEarthRadii.toFloat())
            val circle2 = Circle(Vector2(parameters.minDistanceFromCenter.toFloat(), 0f), umbraRadius.toFloat())
            val obscuration = Geometry.getIntersectionArea(circle2, circle1) / circle1.area()
            return Eclipse(time.start, time.end, magnitude.toFloat(), obscuration)
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