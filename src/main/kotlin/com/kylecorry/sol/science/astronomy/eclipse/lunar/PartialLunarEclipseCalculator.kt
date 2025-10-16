package com.kylecorry.sol.science.astronomy.eclipse.lunar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import com.kylecorry.sol.math.SolMath.power
import kotlin.time.Duration
import kotlin.math.sqrt

internal class PartialLunarEclipseCalculator : AbstractUmbralLunarEclipseCalculator() {

    override fun getMagnitudeThreshold(): Double {
        return 0.0
    }

    override fun getSemiDuration(parameters: LunarEclipseParameters): Duration {
        val p = 1.0128 - parameters.umbralConeRadius
        val minutes =
            (60 / parameters.n) * sqrt(power(p, 2) - power(parameters.minDistanceFromCenter, 2))
        return ((minutes * 60).seconds.toLong())
    }
}