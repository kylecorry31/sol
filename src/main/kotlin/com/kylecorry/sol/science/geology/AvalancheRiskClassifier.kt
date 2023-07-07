package com.kylecorry.sol.science.geology

import kotlin.math.absoluteValue

internal class AvalancheRiskClassifier {

    fun classify(inclination: Float): AvalancheRisk {

        val absAngle = inclination.absoluteValue

        return when {
            absAngle < 30 || absAngle > 60 -> {
                AvalancheRisk.Low
            }
            absAngle in 30.0..45.0 -> {
                AvalancheRisk.High
            }
            else -> {
                AvalancheRisk.Moderate
            }
        }
    }

}