package com.kylecorry.sol.science.astronomy.eclipse.lunar
import com.kylecorry.sol.math.SolMath.power
import java.time.Duration
import kotlin.math.sqrt

internal class PartialLunarEclipseCalculator : AbstractUmbralLunarEclipseCalculator() {

    override fun getMagnitudeThreshold(): Double {
        return 0.0
    }

    override fun getSemiDuration(parameters: LunarEclipseParameters): Duration {
        val p = 1.0128 - parameters.umbralConeRadius
        val minutes =
            (60 / parameters.n) * sqrt(power(p, 2) - power(parameters.minDistanceFromCenter, 2))
        return Duration.ofSeconds((minutes * 60).toLong())
    }
}