package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Angle
import kotlin.math.absoluteValue

internal class AvalancheRiskClassifier {

    fun classify(inclination: Angle): AvalancheRisk {

        val absAngle = inclination.degrees().value.absoluteValue

        return when (absAngle) {
            !in 30.0..60.0 -> {
                AvalancheRisk.Low
            }
            in 30.0..45.0 -> {
                AvalancheRisk.High
            }
            else -> {
                AvalancheRisk.Moderate
            }
        }
    }

}