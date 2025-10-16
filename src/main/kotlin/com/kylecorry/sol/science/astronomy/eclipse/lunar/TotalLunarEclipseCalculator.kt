package com.kylecorry.sol.science.astronomy.eclipse.lunar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

import com.kylecorry.sol.math.SolMath.power
import kotlin.time.Duration
import kotlin.math.sqrt

internal class TotalLunarEclipseCalculator : AbstractUmbralLunarEclipseCalculator() {

    override fun getMagnitudeThreshold(): Double {
        return 1.0
    }

    override fun getSemiDuration(parameters: LunarEclipseParameters): Duration {
        val t = 0.4678 - parameters.umbralConeRadius
        val minutes =
            (60 / parameters.n) * sqrt(power(t, 2) - power(parameters.minDistanceFromCenter, 2))
        return ((minutes * 60).seconds.toLong())
    }
}