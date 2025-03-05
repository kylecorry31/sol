package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.geometry.Geometry
import com.kylecorry.sol.math.regression.WeightedLeastSquaresRegression
import com.kylecorry.sol.math.statistics.Statistics
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
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
 * @param distanceFn
 */
class LoessFilter(
    private val span: Float = 0.3f,
    private val robustnessIterations: Int = 2,
    private val accuracy: Float = 1e-12f,
    private val minimumSpanSize: Int = 0,
    private val maximumSpanSize: Int = Int.MAX_VALUE,
    private val maximumSpanDistance: Float? = null,
    private val distanceFn: (p1: List<Float>, p2: List<Float>) -> Float = Geometry::manhattanDistance
) {

    /**
     * Smooth the data, the output will have the same indices as the input
     */
    fun filter(xs: List<List<Float>>, ys: List<Float>): List<Float> {
        val n = xs.size
        if (n < 3) {
            return ys
        }

        val weights = MutableList(n) { 1f }
        val res = ys.toMutableList()
        val residuals = MutableList(n) { 0f }
        val robustnessWeights = MutableList(n) { 1f }

        for (iteration in 0..robustnessIterations) {
            for (i in ys.indices) {
                val x = xs[i]
                val y = ys[i]

                val nearest = getNearest(xs, ys, i)

                val maxDistance = nearest.lastOrNull()?.second ?: 0f

                if (maxDistance == 0f) {
                    res[i] = y
                    residuals[i] = abs(y - res[i])
                } else {

                    val w = nearest.map {
                        tricube(robustnessWeights[it.third] * weights[it.third] * it.second / maxDistance)
                    }

                    val regression = WeightedLeastSquaresRegression(
                        nearest.map { it.first.first },
                        nearest.map { it.first.second },
                        w
                    )

                    res[i] = regression.predict(x)
                    residuals[i] = abs(y - res[i])
                }
            }

            if (iteration == robustnessIterations) {
                break
            }

            val medianResidual = Statistics.median(residuals)

            if (abs(medianResidual) < accuracy) {
                break
            }

            for (i in ys.indices) {
                val a = residuals[i] / (6 * medianResidual)
                robustnessWeights[i] = if (a >= 1) {
                    0f
                } else {
                    (1 - a * a).pow(2)
                }
            }
        }

        return res
    }

    private fun getNearest(
        xs: List<List<Float>>,
        ys: List<Float>,
        i: Int
    ): List<Triple<Pair<List<Float>, Float>, Float, Int>> {
        val size = floor(span * xs.size).toInt().coerceIn(minimumSpanSize, maximumSpanSize)
        val selected = xs[i]
        return xs.zip(ys)
            .mapIndexed { index, value ->
                Triple(value, distanceFn(selected, value.first), index)
            }
            .sortedBy { it.second }
            .filterNot { it.third == i }
            .take(size)
    }


    private fun tricube(x: Float): Float {
        if (abs(x) >= 1f) {
            return 0f
        }
        return (1 - x.pow(3)).pow(3)
    }

}