package com.kylecorry.trailsensecore.domain.inclinometer

import com.kylecorry.andromeda.core.math.tanDegrees
import kotlin.math.absoluteValue
import kotlin.math.max

class InclinationService : IInclinationService {

    private val riskClassifier = AvalancheRiskClassifier()
    private val heightCalculator = HeightCalculator()

    override fun getAvalancheRisk(inclination: Float): AvalancheRisk {
        return riskClassifier.classify(inclination)
    }

    override fun estimateHeight(distance: Float, inclination: Float, phoneHeight: Float): Float {
        return heightCalculator.calculate(distance, inclination, phoneHeight)
    }

    override fun estimateHeightAngles(
        distance: Float,
        bottomInclination: Float,
        topInclination: Float
    ): Float {

        if (bottomInclination.absoluteValue == 90f || topInclination.absoluteValue == 90f){
            return Float.POSITIVE_INFINITY
        }


        val up = tanDegrees(topInclination) * distance
        val down = tanDegrees(bottomInclination) * distance
        return max(0f, up - down)
    }


}