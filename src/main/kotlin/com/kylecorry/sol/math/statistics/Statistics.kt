package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.RoundingMethod
import com.kylecorry.sol.math.MathExtensions.round
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.multiply
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.filters.MovingAverageFilter
import com.kylecorry.sol.math.interpolation.Interpolation.lerp
import com.kylecorry.sol.math.regression.LinearRegression
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.*

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

    fun meanVector2(values: List<Vector2>): Vector2 {
        if (values.isEmpty()) {
            return Vector2(Float.NaN, Float.NaN)
        }

        var x = 0.0
        var y = 0.0
        for (value in values) {
            x += value.x
            y += value.y
        }
        return Vector2((x / values.size).toFloat(), (y / values.size).toFloat())
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
            Arithmetic.power((it - average) / deviation.toDouble(), 3)
        }.toFloat() / values.size
    }

    fun probability(values: List<Float>): List<Float> {
        val sum = values.sum()
        if (Arithmetic.isZero(sum)) {
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
        return exponents.map { if (Arithmetic.isZero(sumExp)) 0f else it / sumExp }
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
        val correct = confusion.multiply(Matrix.identity(confusion.rows())).sum()
        return correct / total
    }

    /**
     * Calculates the F1 score given a confusion matrix (rows = predicted, columns = actual)
     * @param weighted if true, a weighted average will be used to combine f1 scores by number of examples per class
     */
    fun f1Score(confusion: Matrix, weighted: Boolean = false): Float {
        val all = confusion.sum()
        val weight = 1 / confusion.rows().toFloat()

        var total = 0.0
        for (row in 0 until confusion.rows()) {
            val subtotal = confusion.getColumn(row).sum()
            total += f1Score(confusion, row) * if (weighted) subtotal / all else weight
        }
        return total.toFloat()
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
        val actual = confusion.getColumn(classIdx)
        val tp = confusion[classIdx, classIdx]
        val fn = actual.sum() - tp
        if (Arithmetic.isZero(tp + fn)) {
            return 0f
        }
        return tp / (tp + fn)
    }

    /**
     * Calculates the precision for a single class given a confusion matrix (rows = predicted, columns = actual)
     */
    fun precision(confusion: Matrix, classIdx: Int): Float {
        val predicted = confusion.getRow(classIdx)
        val tp = confusion[classIdx, classIdx]
        val fp = predicted.sum() - tp
        if (Arithmetic.isZero(tp + fp)) {
            return 0f
        }
        return tp / (tp + fp)
    }

    fun smooth(data: List<Float>, smoothing: Float = 0.5f): List<Float> {
        if (data.isEmpty()) {
            return data
        }

        val filter = LowPassFilter(smoothing, data.first())

        return data.mapIndexed { index, value ->
            if (index == 0) {
                value
            } else {
                filter.filter(value)
            }
        }
    }

    fun movingAverage(data: List<Float>, window: Int = 5): List<Float> {
        val filter = MovingAverageFilter(window)

        return data.map { filter.filter(it) }
    }

    fun removeOutliers(
        measurements: List<Double>,
        threshold: Double,
        replaceWithAverage: Boolean = false,
        replaceLast: Boolean = false
    ): List<Double> {
        if (measurements.size < 3) {
            return measurements
        }

        val filtered = mutableListOf(measurements.first())

        for (i in 1 until measurements.lastIndex) {
            val before = measurements[i - 1]
            val current = measurements[i]
            val after = measurements[i + 1]

            val last = if (replaceWithAverage) (before + after) / 2 else filtered.last()

            if (current - before > threshold && current - after > threshold) {
                filtered.add(last)
            } else if (current - before < -threshold && current - after < -threshold) {
                filtered.add(last)
            } else {
                filtered.add(current)
            }
        }

        if (replaceLast && abs(filtered.last() - measurements.last()) > threshold) {
            filtered.add(filtered.last())
        } else {
            filtered.add(measurements.last())
        }
        return filtered
    }

    fun textureFeatures(glcm: Matrix): TextureFeatures {
        var entropy = 0f
        var contrast = 0f
        var homogeneity = 0f
        var dissimilarity = 0f
        var angularSecondMoment = 0f
        var meanI = 0f
        var meanJ = 0f
        var maximum = 0f
        var varianceI = 0f
        var varianceJ = 0f
        var correlation = 0f

        // Texture measures and mean
        for (i in 0..<glcm.rows()) {
            for (j in 0..<glcm.columns()) {
                val p = glcm[i, j]
                val ijSquare = square((i - j).toFloat())
                angularSecondMoment += square(p)
                if (p > 0) {
                    entropy += -p * ln(p)
                }
                contrast += ijSquare * p
                homogeneity += p / (1 + ijSquare)
                dissimilarity += p * abs(i - j)
                maximum = max(maximum, p)
                meanI += i * p
                meanJ += j * p
            }
        }

        // Variance calculation
        for (i in 0..<glcm.rows()) {
            for (j in 0..<glcm.columns()) {
                val p = glcm[i, j]
                varianceI += p * square(i - meanI)
                varianceJ += p * square(j - meanJ)
                correlation += p * (i - meanI) * (j - meanJ)
            }
        }

        // Correlation calculation
        val denominator = sqrt(varianceI * varianceJ)
        if (denominator != 0f) {
            correlation /= denominator
        }

        return TextureFeatures(
            sqrt(angularSecondMoment),
            entropy,
            contrast,
            homogeneity,
            dissimilarity,
            angularSecondMoment,
            meanI,
            meanJ,
            varianceI,
            varianceJ,
            correlation,
            maximum
        )
    }
}