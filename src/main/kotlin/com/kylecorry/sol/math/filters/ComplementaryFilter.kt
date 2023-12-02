package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.sumOfFloat

class ComplementaryFilter(initialWeights: List<Float>, initialValue: Float = 0f) {
    var value = initialValue

    var weights = normalizeWeights(initialWeights)
        set(value) {
            field = normalizeWeights(value)
        }

    fun filter(values: List<Float>): Float {
        value = weightedAverage(weights, values)
        return value
    }

    private fun weightedAverage(weights: List<Float>, values: List<Float>): Float {
        var sum = 0f
        for (i in weights.indices) {
            sum += weights[i] * values[i]
        }
        return sum
    }

    private fun normalizeWeights(weights: List<Float>): List<Float> {
        val sum = weights.sumOfFloat { it }
        return weights.map { it / sum }
    }

}