package com.kylecorry.trailsensecore.astronomy.eclipse

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.astronomy.AstronomyService
import com.kylecorry.trailsensecore.time.InstantRange
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.math.absoluteValue

abstract class AbstractUmbralLunarEclipseCalculator : EclipseCalculator {
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

        val time = InstantRange(
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

        val astronomyService = AstronomyService()
        val upAtStart = astronomyService.isMoonUp(time.start.atZone(ZoneId.of("UTC")), location)
        val upAtEnd = astronomyService.isMoonUp(time.end.atZone(ZoneId.of("UTC")), location)


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