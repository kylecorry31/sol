package com.kylecorry.trailsensecore.domain.inclinometer

import kotlin.math.absoluteValue

internal class AvalancheRiskClassifier {

    fun classify(inclination: Float): AvalancheRisk {

        val absAngle = inclination.absoluteValue

        return when {
            absAngle < 20 -> {
                AvalancheRisk.Low
            }
            absAngle in 30.0..50.0 -> {
                AvalancheRisk.High
            }
            else -> {
                AvalancheRisk.Moderate
            }
        }
    }

}