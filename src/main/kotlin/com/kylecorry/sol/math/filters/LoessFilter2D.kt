package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.interpolation.Interpolation
import com.kylecorry.sol.math.lists.Lists
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.ulp

// Based on org.apache.commons.math.analysis.interpolation.LoessInterpolator
// from http://commons.apache.org/math/

/**
 * A filter for smoothing data
 *
 * Smoothing a point costs work proportional to the number of points in its span, so the total work
 * is proportional to the input size times the span size. With the default proportional [span] that
 * is quadratic in the input size. To bound the work for large inputs, limit the span with
 * [maximumSpanSize] (a point count) and/or [maximumSpanDistance] (an X distance). Both limits
 * smooth each point using a smaller neighborhood than [span] alone would, so they change the output.
 *
 * @param span the percentage of the dataset to use for smoothing each point
 * @param robustnessIterations the number of iterations to do for the robustness step for outlier removal
 * @param accuracy the threshold to stop the robustness at (short circuit)
 * @param minimumSpanSize the minimum number of points to be considered in the span. Must not exceed
 * [maximumSpanSize] (an IllegalArgumentException is thrown when filtering otherwise). It does not guarantee
 * that many points are weighted when [maximumSpanDistance] is set, so keep the distance wide enough to
 * cover several points.
 * @param maximumSpanSize the maximum number of points to be considered in the span. Bounds the work per point
 * regardless of how the points are distributed in X.
 * @param maximumSpanDistance the maximum X distance of the span. Points farther than this from the point being
 * smoothed get no weight and are not visited, so it bounds the work per point to the points within that distance.
 * Unlike [maximumSpanSize], it is independent of how densely the data was sampled.
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
        // Note: This has essentially the same logic as LoessFilter, except there are a few
        // performance optimizations here for the 2D case
        val n = data.size
        if (n < 3) {
            return data
        }

        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        for (point in data) {
            minX = minOf(minX, point.x)
            maxX = maxOf(maxX, point.x)
            minY = minOf(minY, point.y)
            maxY = maxOf(maxY, point.y)
        }

        val sortOrder = if (Lists.isIncreasingX(data)) null else Lists.sortIndices(data.map { it.x })

        // The points are normalized so the smoothing is independent of the units of X and Y
        val xs = FloatArray(n)
        val ys = FloatArray(n)
        for (i in 0 until n) {
            val point = data[sortOrder?.get(i) ?: i]
            xs[i] = Interpolation.norm(point.x, minX, maxX)
            ys[i] = Interpolation.norm(point.y, minY, maxY)
        }

        val state = SmoothingState(
            xs,
            ys,
            spanSize = floor(span * n).toInt().coerceIn(minimumSpanSize, maximumSpanSize),
            maxDistance = maximumSpanDistance?.let { normalizeDistance(it, maxX - minX) }
        )

        for (iteration in 0..robustnessIterations) {
            smoothIteration(state)

            if (iteration == robustnessIterations) {
                break
            }

            val isWithinAccuracy = updateRobustnessWeights(state)
            if (isWithinAccuracy) {
                break
            }
        }

        return buildOutput(data, sortOrder, state.result, minY, maxY)
    }

    // A distance is scaled like the X values, but not shifted by their minimum
    private fun normalizeDistance(distance: Float, rangeX: Float): Float {
        return if (Arithmetic.isZero(rangeX)) 0f else abs(distance / rangeX)
    }

    private fun buildOutput(
        data: List<Vector2>,
        sortOrder: List<Int>?,
        result: FloatArray,
        minY: Float,
        maxY: Float
    ): List<Vector2> {
        val output = arrayOfNulls<Vector2>(data.size)
        for (i in result.indices) {
            val index = sortOrder?.get(i) ?: i
            output[index] = Vector2(data[index].x, Interpolation.lerp(result[i], minY, maxY))
        }
        @Suppress("UNCHECKED_CAST")
        val filtered = output as Array<Vector2>
        return filtered.asList()
    }

    private fun smoothIteration(state: SmoothingState) {
        val xs = state.xs
        val ys = state.ys
        val robustnessWeights = state.robustnessWeights
        val window = IntArray(2)

        for (i in xs.indices) {
            selectWindow(xs, i, state.spanSize, state.windowLimit, window)
            val start = window[0]
            val end = window[1]

            if (end - start < 2) {
                continue
            }

            val x = xs[i]
            val maxDistance = state.maxDistance ?: max(abs(x - xs[start]), abs(xs[end - 1] - x))
            // With no distance to scale by, every point in the span is weighted equally
            val isUniform = Arithmetic.isZero(maxDistance)
            val inverseMaxDistance = 1.0 / maxDistance.toDouble()

            // The regression is centered on the point being smoothed, so the prediction is the
            // intercept and the sums stay well conditioned for dense, narrow spans.
            var sumWeights = 0.0
            var sumX = 0.0
            var sumXSquared = 0.0
            var sumY = 0.0
            var sumXY = 0.0
            for (j in start until end) {
                val dx = xs[j].toDouble() - x
                val weight = if (isUniform) {
                    1.0
                } else {
                    tricube(abs(dx) * inverseMaxDistance) * robustnessWeights[j]
                }
                if (weight <= 0.0) {
                    continue
                }
                val y = ys[j]
                val wx = weight * dx
                sumWeights += weight
                sumX += wx
                sumXSquared += wx * dx
                sumY += weight * y
                sumXY += wx * y
            }

            if (sumWeights <= 0.0) {
                continue
            }

            val meanX = sumX / sumWeights
            val meanY = sumY / sumWeights
            val varianceX = sumXSquared / sumWeights - meanX * meanX
            val slope = if (varianceX <= 0.0 || sqrt(varianceX) < accuracy) {
                0.0
            } else {
                (sumXY / sumWeights - meanX * meanY) / varianceX
            }

            val prediction = (meanY - slope * meanX).toFloat()
            state.result[i] = prediction
            state.residuals[i] = abs(ys[i] - prediction)
        }
    }

    /**
     * Selects the span of the point at index i as the half-open index range [window] (start, end).
     * The span grows toward whichever side's boundary point is closer to the point, until it
     * covers spanSize indices. Growth stops early once both sides are at least maxDistance
     * away, since the points beyond that are given no weight.
     */
    private fun selectWindow(xs: FloatArray, i: Int, spanSize: Int, maxDistance: Float, window: IntArray) {
        val last = xs.lastIndex
        val x = xs[i]
        var start = i
        var end = i
        while (end - start < spanSize) {
            // A side that has run out of points is infinitely far away
            val dStart = if (start > 0) abs(xs[start] - x) else Float.POSITIVE_INFINITY
            val dEnd = if (end < last) abs(xs[end] - x) else Float.POSITIVE_INFINITY
            if (min(dStart, dEnd) >= maxDistance) {
                break
            }

            if (dStart <= dEnd) {
                start--
            } else {
                end++
            }
        }
        window[0] = start
        window[1] = end
    }

    private fun updateRobustnessWeights(state: SmoothingState): Boolean {
        val sortedResiduals = state.residuals.sortedArray()
        val medianResidual = sortedResiduals[(sortedResiduals.lastIndex * 0.5f).toInt()]

        // Residuals below the resolution of the data are rounding noise. When most points fit
        // exactly, the median alone would be zero and outliers would never be rejected.
        if (sortedResiduals.last() <= max(accuracy, NOISE)) {
            return true
        }

        val scale = 6 * max(medianResidual, NOISE)
        for (i in state.residuals.indices) {
            val a = state.residuals[i] / scale
            state.robustnessWeights[i] = if (a >= 1) {
                0f
            } else {
                val b = 1 - a * a
                b * b
            }
        }

        return false
    }

    private fun tricube(x: Double): Double {
        if (x >= 1.0) {
            return 0.0
        }
        val a = 1 - x * x * x
        return a * a * a
    }

    private companion object {
        // The resolution of the normalized Y values
        val NOISE = 1f.ulp
    }

    private class SmoothingState(
        val xs: FloatArray,
        val ys: FloatArray,
        val spanSize: Int,
        val maxDistance: Float?
    ) {
        val result = ys.copyOf()
        val residuals = FloatArray(xs.size)
        val robustnessWeights = FloatArray(xs.size) { 1f }

        // A zero distance gives every point in the span equal weight, so it can't limit the span
        val windowLimit = maxDistance?.takeUnless { Arithmetic.isZero(it) } ?: Float.POSITIVE_INFINITY
    }

}
