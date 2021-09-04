package com.kylecorry.trailsensecore.astronomy.eclipse

import com.kylecorry.andromeda.core.math.power
import java.time.Duration
import kotlin.math.sqrt

class TotalLunarEclipseCalculator : AbstractUmbralLunarEclipseCalculator() {

    override fun getMagnitudeThreshold(): Double {
        return 1.0
    }

    override fun getSemiDuration(parameters: LunarEclipseParameters): Duration {
        val t = 0.4678 - parameters.umbralConeRadius
        val minutes =
            (60 / parameters.n) * sqrt(power(t, 2) - power(parameters.minDistanceFromCenter, 2))
        return Duration.ofSeconds((minutes * 60).toLong())
    }
}