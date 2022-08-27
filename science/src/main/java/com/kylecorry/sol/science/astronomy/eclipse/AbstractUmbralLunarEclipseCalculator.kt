package com.kylecorry.sol.science.astronomy.eclipse

import com.kylecorry.sol.science.astronomy.Astronomy
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
        val magnitude =
            (1.0128 - parameters.umbralConeRadius - parameters.minDistanceFromCenter.absoluteValue) / 0.545

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

        val time = com.kylecorry.sol.time.InstantRange(
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
            return Eclipse(time.start, time.end, magnitude.toFloat())
        }

        return getNextEclipseHelper(
            parameters.maximum.plus(Duration.ofDays(10)),
            atLeastInstant,
            location,
            iterationsRemaining - 1
        )
    }

}