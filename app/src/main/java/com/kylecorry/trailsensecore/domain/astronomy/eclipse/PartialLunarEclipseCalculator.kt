package com.kylecorry.trailsensecore.domain.astronomy.eclipse

import com.kylecorry.andromeda.core.math.power
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.InstantRange
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class PartialLunarEclipseCalculator : EclipseCalculator {
    override fun getNextEclipse(after: Instant, location: Coordinate): InstantRange? {
        val provider = LunarEclipseParameterProvider()

        var parameters: LunarEclipseParameters
        var magnitude: Double
        var date = after
        var iterations = 0

        // TODO: Determine if it can be seen from the location (i.e. moon is above horizon at some point during the time range)
        do {
            parameters = provider.getNextLunarEclipseParameters(date)
            magnitude =
                (1.0128 - parameters.umbralConeRadius - parameters.minDistanceFromCenter.absoluteValue) / 0.545
            if (magnitude < 0) {
                date = parameters.maximum.plus(Duration.ofDays(10))
            }
            iterations++
            if (iterations > 100){
                return null
            }
        } while (magnitude < 0)

        val p = 1.0128 - parameters.umbralConeRadius
        val semiduration =
            (60 / parameters.n) * sqrt(power(p, 2) - power(parameters.minDistanceFromCenter, 2))

        return InstantRange(
            parameters.maximum - Duration.ofSeconds((semiduration * 60).toLong()),
            parameters.maximum + Duration.ofSeconds((semiduration * 60).toLong()),
        )
    }
}