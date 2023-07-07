package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.sumOfFloat

class ComplementaryFilter(private val weights: List<Float>, initialValue: Float = 0f) {
    var value = initialValue
        private set

    fun filter(values: List<Float>): Float {
        value = weights.zip(values).sumOfFloat { it.first * it.second }
        return value
    }

}