package com.kylecorry.trailsensecore.domain.inclinometer

class InclinationService {

    private val riskClassifier = AvalancheRiskClassifier()
    private val heightCalculator = HeightCalculator()

    fun getAvalancheRisk(inclination: Float): AvalancheRisk {
        return riskClassifier.classify(inclination)
    }

    fun estimateHeight(
        distanceAwayMeters: Float,
        inclination: Float,
        phoneHeightMeters: Float
    ): Float {
        return heightCalculator.calculate(distanceAwayMeters, inclination, phoneHeightMeters)
    }


}