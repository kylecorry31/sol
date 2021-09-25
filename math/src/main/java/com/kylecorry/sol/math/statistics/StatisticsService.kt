package com.kylecorry.sol.math.statistics

import kotlin.math.pow

class StatisticsService {

    /**
     * Calculate the weighted sum of the provided values
     * @param values a list of value to weights [0, 1]
     */
    fun weightedSum(values: List<Pair<Float, Float>>): Float {
        var sum = 0f
        for (value in values) {
            sum += value.first * value.second
        }

        return sum
    }

    /**
     * Calculates the slope of the best fit line
     */
    fun slope(data: List<Pair<Float, Float>>): Float {
        if (data.size <= 1) {
            return 0f
        }

        val xBar = data.map { it.first }.average().toFloat()
        val yBar = data.map { it.second }.average().toFloat()

        var ssxx = 0.0f
        var ssxy = 0.0f
        var ssto = 0.0f

        for (i in data.indices) {
            ssxx += (data[i].first - xBar).pow(2)
            ssxy += (data[i].first - xBar) * (data[i].second - yBar)
            ssto += (data[i].second - yBar).pow(2)
        }

        return ssxy / ssxx
    }
}