package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.geometry.Geometry
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.ulp

// Based on org.apache.commons.math.analysis.interpolation.LoessInterpolator
// from http://commons.apache.org/math/

/**
 * A filter for smoothing data
 *
 * Each point is smoothed using its nearest neighbors (excluding itself), so the distance from every
 * point to every other point is calculated. The work is therefore at least quadratic in the input
 * size, even if the span is limited with [maximumSpanSize].
 *
 * @param span the percentage of the dataset to use for smoothing each point
 * @param robustnessIterations the number of iterations to do for the robustness step for outlier removal
 * @param accuracy the threshold to stop the robustness at (short circuit)
 * @param minimumSpanSize the minimum number of points to be considered in the span
 * @param maximumSpanSize the maximum number of points to be considered in the span
 * @param distanceFn the distance between two points, it is called with the point being smoothed first
 */
class LoessFilter(
    private val span: Float = 0.3f,
    private val robustnessIterations: Int = 2,
    private val accuracy: Float = 1e-12f,
    private val minimumSpanSize: Int = 0,
    private val maximumSpanSize: Int = Int.MAX_VALUE,
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

        val state = SmoothingState(
            xs,
            ys,
            spanSize = floor(span * n).toInt().coerceIn(minimumSpanSize, maximumSpanSize).coerceAtMost(n - 1)
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

        return state.result.asList()
    }

    private fun smoothIteration(state: SmoothingState) {
        val xs = state.xs
        val ys = state.ys
        val dimensions = state.dimensions
        val neighbors = IntArray(state.spanSize)
        val distances = FloatArray(xs.size)
        val scratch = FloatArray(xs.size)
        val equations = NormalEquations(dimensions + 1)

        for (i in xs.indices) {
            val x = xs[i]
            val y = ys[i]

            val maxDistance = selectNeighbors(state, i, neighbors, distances, scratch)

            if (Arithmetic.isZero(maxDistance)) {
                state.result[i] = y
                state.residuals[i] = 0f
                continue
            }

            // The regression is centered on the point being smoothed, so the prediction is the
            // intercept and the normal equations stay well conditioned.
            equations.reset()
            var weightedPoints = 0
            for (j in neighbors) {
                val weight = tricube(distances[j] / maxDistance.toDouble()) * state.robustnessWeights[j]
                if (weight <= 0.0) {
                    continue
                }
                weightedPoints++
                equations.add(xs[j], x, ys[j], weight)
            }

            if (weightedPoints < dimensions + 1) {
                continue
            }

            val prediction = equations.solveIntercept().toFloat()
            state.result[i] = prediction
            state.residuals[i] = abs(y - prediction)
        }
    }

    /**
     * Fills neighbors with the indices of the spanSize points nearest to point i (not including i),
     * preferring the lower index when points are the same distance away. The distances to every point
     * are left in distances.
     * @return the distance to the farthest neighbor, or 0 if there are no neighbors
     */
    private fun selectNeighbors(
        state: SmoothingState,
        i: Int,
        neighbors: IntArray,
        distances: FloatArray,
        scratch: FloatArray
    ): Float {
        val k = neighbors.size
        if (k == 0) {
            return 0f
        }

        val x = state.xs[i]
        for (j in distances.indices) {
            distances[j] = if (j == i) Float.POSITIVE_INFINITY else distanceFn(x, state.xs[j])
        }

        distances.copyInto(scratch)
        val maxDistance = kthSmallest(scratch, k - 1)

        var count = 0
        for (j in distances.indices) {
            if (distances[j] < maxDistance) {
                neighbors[count++] = j
            }
        }
        for (j in distances.indices) {
            if (count == k) {
                break
            }
            if (distances[j] == maxDistance && j != i) {
                neighbors[count++] = j
            }
        }
        return maxDistance
    }

    // Quickselect, rearranges values
    private fun kthSmallest(values: FloatArray, k: Int): Float {
        var low = 0
        var high = values.lastIndex
        while (low < high) {
            val pivot = values[(low + high) ushr 1]
            var a = low
            var b = high
            while (a <= b) {
                while (values[a] < pivot) a++
                while (values[b] > pivot) b--
                if (a <= b) {
                    val temp = values[a]
                    values[a] = values[b]
                    values[b] = temp
                    a++
                    b--
                }
            }
            if (k <= b) {
                high = b
            } else if (k >= a) {
                low = a
            } else {
                break
            }
        }
        return values[k]
    }

    private fun updateRobustnessWeights(state: SmoothingState): Boolean {
        val sortedResiduals = state.residuals.sortedArray()
        val medianResidual = sortedResiduals[(sortedResiduals.lastIndex * 0.5f).toInt()]

        // Residuals below the resolution of the data are rounding noise. When most points fit
        // exactly, the median alone would be zero and outliers would never be rejected.
        val noise = state.noise
        if (sortedResiduals.last() <= max(accuracy, noise)) {
            return true
        }

        val scale = 6 * max(medianResidual, noise)
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
        // The determinant of the scaled normal equations is 1 for independent variables and 0 for dependent ones
        const val DEGENERATE_DETERMINANT = 1e-10
    }

    private class SmoothingState(
        val xs: List<List<Float>>,
        val ys: List<Float>,
        val spanSize: Int
    ) {
        val dimensions = xs[0].size
        val result = ys.toFloatArray()
        val residuals = FloatArray(xs.size)
        val robustnessWeights = FloatArray(xs.size) { 1f }
        val noise = ys.maxOf { abs(it) }.ulp
    }

    /**
     * The weighted least squares normal equations for a plane, stored with only the lower triangle of the
     * matrix filled until solved.
     */
    private class NormalEquations(private val size: Int) {
        private val matrix = DoubleArray(size * size)
        private val vector = DoubleArray(size)

        fun reset() {
            matrix.fill(0.0)
            vector.fill(0.0)
        }

        fun add(point: List<Float>, center: List<Float>, y: Float, weight: Double) {
            val last = size - 1
            for (r in 0 until size) {
                val row = if (r == last) 1.0 else point[r].toDouble() - center[r]
                val weightedRow = weight * row
                for (c in 0..r) {
                    val column = if (c == last) 1.0 else point[c].toDouble() - center[c]
                    matrix[r * size + c] += weightedRow * column
                }
                vector[r] += weightedRow * y
            }
        }

        /**
         * Solves for the last coefficient, which is the intercept. If the points are degenerate (ex. all in
         * a line), the weighted mean of y is used instead. The equations can't be reused until reset.
         */
        fun solveIntercept(): Double {
            val last = size - 1
            val mean = vector[last] / matrix[last * size + last]

            // Scale each variable by its spread so the check for degeneracy doesn't depend on the units
            val scales = DoubleArray(size) { sqrt(matrix[it * size + it]) }
            val determinant = if (scales.any { it == 0.0 }) {
                0.0
            } else {
                scale(scales)
                eliminate()
            }
            if (abs(determinant) < DEGENERATE_DETERMINANT) {
                return mean
            }

            backSubstitute()
            return vector[last] / scales[last]
        }

        private fun scale(scales: DoubleArray) {
            for (r in 0 until size) {
                for (c in 0..r) {
                    matrix[r * size + c] /= scales[r] * scales[c]
                    matrix[c * size + r] = matrix[r * size + c]
                }
                vector[r] /= scales[r]
            }
        }

        /**
         * Reduces the matrix to upper triangular form with partial pivoting.
         * @return the determinant of the matrix, 0 if it is singular
         */
        private fun eliminate(): Double {
            var determinant = 1.0
            for (column in 0 until size) {
                val pivot = findPivot(column)
                val pivotValue = matrix[pivot * size + column]
                if (pivotValue == 0.0) {
                    return 0.0
                }

                if (pivot != column) {
                    swapRows(column, pivot)
                    determinant = -determinant
                }
                determinant *= pivotValue

                for (r in column + 1 until size) {
                    val factor = matrix[r * size + column] / pivotValue
                    for (c in column until size) {
                        matrix[r * size + c] -= factor * matrix[column * size + c]
                    }
                    vector[r] -= factor * vector[column]
                }
            }
            return determinant
        }

        private fun findPivot(column: Int): Int {
            var pivot = column
            for (r in column + 1 until size) {
                if (abs(matrix[r * size + column]) > abs(matrix[pivot * size + column])) {
                    pivot = r
                }
            }
            return pivot
        }

        private fun swapRows(a: Int, b: Int) {
            for (c in 0 until size) {
                val temp = matrix[a * size + c]
                matrix[a * size + c] = matrix[b * size + c]
                matrix[b * size + c] = temp
            }
            val temp = vector[a]
            vector[a] = vector[b]
            vector[b] = temp
        }

        private fun backSubstitute() {
            for (r in size - 1 downTo 0) {
                var sum = vector[r]
                for (c in r + 1 until size) {
                    sum -= matrix[r * size + c] * vector[c]
                }
                vector[r] = sum / matrix[r * size + r]
            }
        }
    }

}
