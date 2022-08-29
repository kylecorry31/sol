package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.LinearEquation
import kotlin.math.abs
import kotlin.math.sqrt

class WeightedLinearRegression(data: List<Vector2>, weights: List<Float>, accuracy: Float = 0f) :
    IRegression {

    val equation = fit(data, weights, accuracy)

    override fun predict(x: Float): Float {
        return equation.evaluate(x)
    }

    private fun fit(data: List<Vector2>, weights: List<Float>, accuracy: Float): LinearEquation {
        if (data.size <= 1) {
            return LinearEquation(0f, 0f)
        }

        var sumWeights = 0f
        var sumX = 0f
        var sumXSquared = 0f
        var sumY = 0f
        var sumXY = 0f

        for (i in data.indices) {
            val xi = data[i].x
            val yi = data[i].y
            val w = weights[i]
            val xiw = xi * w

            sumWeights += w
            sumX += xiw
            sumXSquared += xi * xiw
            sumY += yi * w
            sumXY += yi * xiw
        }

        val meanX = sumX / sumWeights
        val meanY = sumY / sumWeights
        val meanXY = sumXY / sumWeights
        val meanXSquared = sumXSquared / sumWeights

        val slope = if (sqrt(
                abs(meanXSquared - meanX * meanX).toDouble()
            ) < accuracy
        ) 0f else (meanXY - meanX * meanY) / (meanXSquared - meanX * meanX)

        val intercept = meanY - slope * meanX
        return LinearEquation(slope, intercept)
    }

}