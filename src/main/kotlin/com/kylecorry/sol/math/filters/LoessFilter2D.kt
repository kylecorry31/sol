package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.interpolation.Interpolation
import com.kylecorry.sol.math.lists.Lists
import com.kylecorry.sol.math.regression.WeightedLinearRegression
import com.kylecorry.sol.math.statistics.Statistics
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

// Based on org.apache.commons.math.analysis.interpolation.LoessInterpolator
// from http://commons.apache.org/math/

/**
 * A filter for smoothing data
 * @param span the percentage of the dataset to use for smoothing each point
 * @param robustnessIterations the number of iterations to do for the robustness step for outlier removal
 * @param accuracy the threshold to stop the robustness at (short circuit)
 * @param minimumSpanSize the minimum number of points to be considered in the span
 * @param maximumSpanSize the maximum number of points to be considered in the span
 * @param maximumSpanDistance the maximum X distance of the span
 */
class LoessFilter2D(
    private val span: Float = 0.3f,
    private val robustnessIterations: Int = 2,
    private val accuracy: Float = 1e-12f,
    private val minimumSpanSize: Int = 0,
    private val maximumSpanSize: Int = Int.MAX_VALUE,
    private val maximumSpanDistance: Float? = null
) : IFilter2D {

    /**
     * Smooth the data, the output will have the same x values as the input
     */
    override fun filter(data: List<Vector2>): List<Vector2> {
        // Note: This has essentially the same logic as LoessFilter, except there are a few performance optimizations here for the 2D case
        val n = data.size
        if (n < 3) {
            return data
        }

        val rangeX = Range(data.minOf { it.x }, data.maxOf { it.x })
        val rangeY = Range(data.minOf { it.y }, data.maxOf { it.y })

        val wasResorted = !Lists.isIncreasingX(data)
        var sortOrder = data.indices.toList()

        val sortedData = if (wasResorted) {
            sortOrder = Lists.sortIndices(data.map { it.x })
            Lists.reorder(data, sortOrder)
        } else {
            data
        }.map {
            Vector2(
                Interpolation.norm(it.x, rangeX.start, rangeX.end),
                Interpolation.norm(it.y, rangeY.start, rangeY.end)
            )
        }

        val weights = MutableList(n) { 1f }
        val result = sortedData.toMutableList()
        val residuals = MutableList(n) { 0f }
        val robustnessWeights = MutableList(n) { 1f }
        val mappedMaxDistance =
            maximumSpanDistance?.let { Interpolation.norm(maximumSpanDistance, rangeX.start, rangeX.end) }
        val state = SmoothingState(weights, result, residuals, robustnessWeights, mappedMaxDistance)

        for (iteration in 0..robustnessIterations) {
            smoothIteration(sortedData, state)

            if (iteration == robustnessIterations) {
                break
            }

            val isWithinAccuracy = updateRobustnessWeights(state)
            if (isWithinAccuracy) {
                break
            }
        }

        return if (wasResorted) {
            Lists.reorder(state.result, sortOrder, true)
        } else {
            state.result
        }.map {
            Vector2(
                Interpolation.lerp(it.x, rangeX.start, rangeX.end),
                Interpolation.lerp(it.y, rangeY.start, rangeY.end)
            )
        }
    }

    private fun smoothIteration(sortedData: List<Vector2>, state: SmoothingState) {
        for (i in sortedData.indices) {
            val point = sortedData[i]
            val interval = getNearest(sortedData, i)

            if (interval.second - interval.first < 2) {
                continue
            }

            val nearest = getNearestPoints(sortedData, point, interval)
            val maxDistance = state.mappedMaxDistance ?: nearest.last().second
            val regressionWeights = getRegressionWeights(nearest, state.robustnessWeights, state.weights, maxDistance)
            val regression = WeightedLinearRegression(nearest.map { it.first }, regressionWeights, accuracy)

            state.result[i] = Vector2(point.x, regression.predict(point.x))
            state.residuals[i] = abs(point.y - state.result[i].y)
        }
    }

    private fun getNearestPoints(
        sortedData: List<Vector2>,
        point: Vector2,
        interval: Pair<Int, Int>
    ): List<Triple<Vector2, Float, Int>> {
        return sortedData.subList(interval.first, interval.second).mapIndexed { index, candidate ->
            Triple(candidate, abs(point.x - candidate.x), interval.first + index)
        }.sortedBy { it.second }
    }

    private fun getRegressionWeights(
        nearest: List<Triple<Vector2, Float, Int>>,
        robustnessWeights: List<Float>,
        weights: List<Float>,
        maxDistance: Float
    ): List<Float> {
        return nearest.map {
            if (Arithmetic.isZero(maxDistance)) {
                1f
            } else {
                tricube(robustnessWeights[it.third] * weights[it.third] * it.second / maxDistance)
            }
        }
    }

    private fun updateRobustnessWeights(state: SmoothingState): Boolean {
        val medianResidual = Statistics.median(state.residuals)
        if (abs(medianResidual) < accuracy) {
            return true
        }

        for (i in state.residuals.indices) {
            val a = state.residuals[i] / (6 * medianResidual)
            state.robustnessWeights[i] = if (a >= 1) {
                0f
            } else {
                (1 - a * a).pow(2)
            }
        }

        return false
    }

    private fun getNearest(points: List<Vector2>, i: Int): Pair<Int, Int> {
        val size = floor(span * points.size).toInt().coerceIn(minimumSpanSize, maximumSpanSize)
        var start = i
        var end = i
        val x = points[i].x
        while ((end - start) < size) {
            val dStart = abs(points[start].x - x)
            val dEnd = abs(points[end].x - x)
            if (start == 0 && end < points.size - 1) {
                end++
            } else if (end == points.size - 1 && start > 0) {
                start--
            } else if (start > 0 && dStart <= dEnd) {
                start--
            } else if (end < points.size - 1 && dEnd <= dStart) {
                end++
            } else {
                break
            }
        }
        return start to end

    }

    private fun tricube(x: Float): Float {
        if (abs(x) >= 1f) {
            return 0f
        }
        return (1 - x.pow(3)).pow(3)
    }

    private data class SmoothingState(
        val weights: List<Float>,
        val result: MutableList<Vector2>,
        val residuals: MutableList<Float>,
        val robustnessWeights: MutableList<Float>,
        val mappedMaxDistance: Float?
    )

}
