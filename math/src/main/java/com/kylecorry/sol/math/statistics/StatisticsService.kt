package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.regression.LinearRegression
import com.kylecorry.sol.math.sumOfFloat
import java.lang.Math.pow
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

class StatisticsService {

    /**
     * Calculate the weighted sum of the provided values
     * @param values a list of value to weights [0, 1]
     */
    fun weightedMean(values: List<Pair<Float, Float>>): Float {
        var sum = 0f
        for (value in values) {
            sum += value.first * value.second
        }

        return sum
    }

    fun mean(values: List<Float>): Float {
        return values.average().toFloat()
    }

    fun geometricMean(values: List<Float>): Float {
        if (values.isEmpty()) {
            return 0f
        }

        var total = 1.0
        for (value in values) {
            total *= value
        }
        return total.pow(1 / values.size.toDouble()).toFloat()
    }

    fun variance(values: List<Float>, forPopulation: Boolean = false, mean: Float? = null): Float {
        if (values.size <= 1) {
            return 0f
        }
        val average = mean?.toDouble() ?: values.average()
        return values.sumOf { square(it.toDouble() - average) }
            .toFloat() / (values.size - if (!forPopulation) 1 else 0)
    }

    fun stdev(values: List<Float>, forPopulation: Boolean = false, mean: Float? = null): Float {
        return sqrt(variance(values, forPopulation, mean))
    }

    fun median(values: List<Float>): Float {
        if (values.isEmpty()) {
            return 0f
        }

        return values.sorted()[values.size / 2]
    }

    fun skewness(
        values: List<Float>,
        mean: Float? = null,
        stdev: Float? = null
    ): Float {
        val average = mean ?: values.average().toFloat()
        val deviation = stdev ?: stdev(values, mean = average)

        return values.sumOf {
            SolMath.power((it - average) / deviation.toDouble(), 3)
        }.toFloat() / values.size
    }

    fun probability(values: List<Float>): List<Float> {
        val sum = values.sum()
        if (sum == 0f) {
            return values
        }

        return values.map { it / sum }
    }

    fun softmax(values: List<Float>): List<Float> {
        if (values.isEmpty()) {
            return emptyList()
        }
        val maxZ = values.max()
        val exponents = values.map { exp(it - maxZ) }
        val sumExp = exponents.sum()
        return exponents.map { if (sumExp == 0f) 0f else it / sumExp }
    }

    /**
     * Calculates the slope of the best fit line
     */
    fun slope(data: List<Vector2>): Float {
        return LinearRegression(data).equation.m
    }

    /**
     * Calculates the root mean square errors between the datasets. Actual and predicted must correspond 1-1 with eachother.
     */
    fun rmse(actual: List<Float>, predicted: List<Float>): Float {
        val n = actual.size
        return sqrt(sse(actual, predicted) / n)
    }

    /**
     * Calculates the sum of squared errors between the datasets. Actual and predicted must correspond 1-1 with eachother.
     */
    fun sse(actual: List<Float>, predicted: List<Float>): Float {

        var sum = 0f
        for (i in actual.indices) {
            sum += (actual[i] - predicted[i]).pow(2)
        }

        return sum
    }
}