package com.kylecorry.trailsensecore.domain.inclinometer

class InclinationService : IInclinationService {

    private val riskClassifier = AvalancheRiskClassifier()
    private val heightCalculator = HeightCalculator()

    override fun getAvalancheRisk(inclination: Float): AvalancheRisk {
        return riskClassifier.classify(inclination)
    }

    override fun estimateHeight(distance: Float, inclination: Float, phoneHeight: Float): Float {
        return heightCalculator.calculate(distance, inclination, phoneHeight)
    }

}