package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.RoundingMethod
import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.lerp
import com.kylecorry.sol.math.SolMath.round
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.*
import com.kylecorry.sol.math.regression.LinearRegression
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Statistics {

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

    fun harmonicMean(values: List<Float>): Float {
        return values.size / values.sumOfFloat { 1 / it }
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
        return quantile(values, 0.5f, interpolate = false)
    }

    fun quantile(values: List<Float>, quantile: Float, interpolate: Boolean = true): Float {
        if (values.isEmpty()) {
            return 0f
        }

        val sorted = values.sorted()

        val idx = sorted.lastIndex * quantile

        if (!interpolate) {
            // Round toward zero to match the behavior of numpy
            return sorted[idx.round(RoundingMethod.TowardZero)]
        }

        // Index is an integer, short circuit
        if (idx == idx.toInt().toFloat()) {
            return sorted[idx.toInt()]
        }

        val lower = sorted[idx.toInt()]
        val upper = sorted[(idx.toInt() + 1).coerceIn(0, sorted.lastIndex)]
        val remainder = idx - idx.toInt()
        return lerp(remainder, lower, upper)
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

    fun probability(x: Float, distribution: GaussianDistribution): Float {
        return distribution.probability(x)
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

    fun joint(distributions: List<GaussianDistribution>): GaussianDistribution? {
        if (distributions.isEmpty()) {
            return null
        }
        var mean = distributions.first().mean
        var variance = distributions.first().variance

        for (i in 1..distributions.lastIndex) {
            val m2 = distributions[i].mean
            val var2 = distributions[i].variance
            val k = variance / (variance + var2)
            mean += k * (m2 - mean)
            variance *= (1 - k)
        }

        return GaussianDistribution(mean, sqrt(variance))
    }

    fun zScore(value: Float, distribution: GaussianDistribution, n: Int = 1): Float {
        return if (n == 1) {
            (value - distribution.mean) / distribution.standardDeviation
        } else {
            (value - distribution.mean) / (distribution.standardDeviation / sqrt(n.toFloat()))
        }
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

    /**
     * Calculates the accuracy given a confusion matrix (rows = predicted, columns = actual)
     */
    fun accuracy(confusion: Matrix): Float {
        val total = confusion.sum()
        val correct = confusion.multiply(identityMatrix(confusion.rows())).sum()
        return correct / total
    }

    /**
     * Calculates the F1 score given a confusion matrix (rows = predicted, columns = actual)
     * @param weighted if true, a weighted average will be used to combine f1 scores by number of examples per class
     */
    fun f1Score(confusion: Matrix, weighted: Boolean = false): Float {
        val all = confusion.sum()
        val weight = 1 / confusion.rows().toFloat()
        return confusion.mapIndexed { index, _ ->
            val total = confusion.transpose()[index].sum()
            f1Score(confusion, index) * if (weighted) total / all else weight
        }.sum()
    }

    /**
     * Calculates the F1 for a single class given a confusion matrix (rows = predicted, columns = actual)
     */
    fun f1Score(confusion: Matrix, classIdx: Int): Float {
        val precision = precision(confusion, classIdx)
        val recall = recall(confusion, classIdx)
        val f = (2 * precision * recall) / (precision + recall)
        if (f.isNaN()) {
            return 0f
        }
        return f
    }

    /**
     * Calculates the recall for a single class given a confusion matrix (rows = predicted, columns = actual)
     */
    fun recall(confusion: Matrix, classIdx: Int): Float {
        val actual = confusion.transpose()[classIdx]
        val tp = confusion[classIdx][classIdx]
        val fn = actual.sum() - tp
        if ((tp + fn) == 0f) {
            return 0f
        }
        return tp / (tp + fn)
    }

    /**
     * Calculates the precision for a single class given a confusion matrix (rows = predicted, columns = actual)
     */
    fun precision(confusion: Matrix, classIdx: Int): Float {
        val predicted = confusion[classIdx]
        val tp = confusion[classIdx][classIdx]
        val fp = predicted.sum() - tp
        if ((tp + fp) == 0f) {
            return 0f
        }
        return tp / (tp + fp)
    }
}