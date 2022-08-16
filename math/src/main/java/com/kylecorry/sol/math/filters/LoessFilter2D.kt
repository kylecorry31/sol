package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.regression.WeightedLinearRegression
import com.kylecorry.sol.math.statistics.StatisticsService
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
 */
class LoessFilter2D(
    private val span: Float = 0.3f,
    private val robustnessIterations: Int = 2,
    private val accuracy: Float = 1e-12f,
    private val minimumSpanSize: Int = 0,
    private val maximumSpanSize: Int = Int.MAX_VALUE
) : IFilter2D {

    private val statistics = StatisticsService()

    /**
     * Smooth the data, the output will have the same x values as the input
     */
    override fun filter(data: List<Vector2>): List<Vector2> {
        val n = data.size
        if (n < 3) {
            return data
        }

        val wasResorted = !SolMath.isIncreasingX(data)
        var sortOrder = data.indices.toList()

        val sortedData = if (wasResorted) {
            sortOrder = SolMath.sortIndices(data.map { it.x })
            SolMath.reorder(data, sortOrder)
        } else {
            data
        }

        val weights = MutableList(n) { 1f }
        val res = sortedData.toMutableList()
        val residuals = MutableList(n) { 0f }
        val robustnessWeights = MutableList(n) { 1f }

        for (iteration in 0..robustnessIterations) {
            for (i in sortedData.indices) {
                val point = sortedData[i]
                val x = point.x
                val y = point.y

                val interval = getNearest(sortedData, i)

                if (interval.second - interval.first < 2) {
                    continue
                }

                val nearest =
                    sortedData.subList(interval.first, interval.second).mapIndexed { index, p ->
                        Triple(p, abs(point.x - p.x), interval.first + index)
                    }.sortedBy { it.second }

                val maxDistance = nearest.last().second

                val w = nearest.map {
                    if (maxDistance == 0f){
                        1f
                    } else {
                        tricube(robustnessWeights[it.third] * weights[it.third] * it.second / maxDistance)
                    }
                }

                val regression = WeightedLinearRegression(nearest.map { it.first }, w, accuracy)

                res[i] = Vector2(x, regression.predict(x))
                residuals[i] = abs(y - res[i].y)
            }

            if (iteration == robustnessIterations) {
                break
            }

            val medianResidual = statistics.median(residuals)

            if (abs(medianResidual) < accuracy) {
                break
            }

            for (i in sortedData.indices) {
                val a = residuals[i] / (6 * medianResidual)
                robustnessWeights[i] = if (a >= 1) {
                    0f
                } else {
                    (1 - a * a).pow(2)
                }
            }
        }

        return if (wasResorted) {
            SolMath.reorder(res, sortOrder, true)
        } else {
            res
        }
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

}