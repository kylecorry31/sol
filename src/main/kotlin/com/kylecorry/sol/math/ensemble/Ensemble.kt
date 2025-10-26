package com.kylecorry.sol.math.ensemble

import com.kylecorry.sol.math.statistics.ConfidenceInterval
import com.kylecorry.sol.math.statistics.Statistics

object Ensemble {
    fun <T> discrete(values: List<T>): List<ConfidenceScore<T>> {
        val total = values.size
        if (total == 0) {
            return emptyList()
        }
        return values
            .groupBy { it }
            .map { ConfidenceScore(it.key, it.value.size / total.toFloat()) }
    }

    fun continuous(values: List<Float>, confidenceIntervalSize: Float = 0.95f): ConfidenceInterval<Float> {
        val median = Statistics.median(values)
        val lower = Statistics.quantile(values, 1f - confidenceIntervalSize)
        val upper = Statistics.quantile(values, confidenceIntervalSize)
        return ConfidenceInterval(median, lower, upper, confidenceIntervalSize)
    }
}