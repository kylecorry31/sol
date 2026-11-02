package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.geometry.Geometry
import kotlin.math.abs
import kotlin.math.sqrt

class LeastSquaresOptimizer {

    /**
     * Minimizes the sum of squared weighted residuals (error - distance) using the Levenberg-Marquardt method.
     * A step is only taken if it lowers the sum of squares, otherwise the damping is increased and it is retried.
     * @param maxAllowedStep the largest change to any value in a single step
     * @param dampingFactor scales each step
     * @throws IllegalStateException if a value does not affect any residual
     */
    fun optimize(
        points: List<List<Float>>,
        errors: List<Float>,
        maxIterations: Int = 100,
        maxAllowedStep: Float = 500f,
        dampingFactor: Float = 0.1f,
        tolerance: Float = 0.0001f,
        initialValue: List<Float>? = null,
        distanceFn: (point: List<Float>, guess: List<Float>) -> Float = { a, b -> Geometry.euclideanDistance(a, b) },
        weightingFn: (index: Int, point: List<Float>, error: Float) -> Float = { index, point, error -> 1f },
        jacobianFn: (index: Int, point: List<Float>, guess: List<Float>) -> List<Float> = { index, point, guess ->
            val distance = distanceFn(point, guess)
            point.mapIndexed { j, value ->
                (guess[j] - value) / distance * weightingFn(index, point, errors[index])
            }
        }
    ): List<Float> {
        if (points.size < 2) {
            return points.firstOrNull().orEmpty()
        }

        require(points.size == errors.size) { "The number of points and errors must be equal." }

        // Initial guess for the observer location (average of the points)
        var guess = initialValue ?: List(points[0].size) { index ->
            points.map { it[index] }.average().toFloat()
        }

        fun residuals(guess: List<Float>): DoubleArray {
            return DoubleArray(points.size) { i ->
                ((errors[i] - distanceFn(points[i], guess)) * weightingFn(i, points[i], errors[i])).toDouble()
            }
        }

        var residuals = residuals(guess)
        var cost = residuals.sumOf { it * it }
        var damping = INITIAL_DAMPING
        var iteration = 0
        var converged = false
        while (iteration < maxIterations && damping <= MAX_DAMPING && !converged) {
            iteration++
            val jacobian = points.mapIndexed { i, point -> jacobianFn(i, point, guess) }
            val candidate = solveDamped(jacobian, residuals, damping)
                ?.let { evaluate(guess, limit(it, maxAllowedStep, dampingFactor), ::residuals) }
            // NaN is never less than the cost, so it is rejected
            if (candidate != null && candidate.cost < cost) {
                guess = candidate.guess
                residuals = candidate.residuals
                cost = candidate.cost
                damping /= DAMPING_SCALE
                converged = candidate.stepSize < tolerance
            } else {
                damping *= DAMPING_SCALE
            }
        }

        return guess
    }

    /**
     * Solves (JᵀJ + damping * diag(JᵀJ)) step = Jᵀr
     */
    private fun solveDamped(jacobian: List<List<Float>>, residuals: DoubleArray, damping: Double): DoubleArray? {
        val n = jacobian.first().size
        val a = Array(n) { DoubleArray(n + 1) }
        for (i in jacobian.indices) {
            val row = jacobian[i]
            for (r in 0 until n) {
                for (c in 0 until n) {
                    a[r][c] += row[r].toDouble() * row[c]
                }
                a[r][n] += row[r] * residuals[i]
            }
        }
        for (i in 0 until n) {
            check(a[i][i] > 0.0) { "Value $i does not affect any residual" }
            a[i][i] *= 1 + damping
        }

        for (col in 0 until n) {
            val pivot = (col until n).maxBy { abs(a[it][col]) }
            if (a[pivot][col] == 0.0) {
                return null
            }
            val tmp = a[pivot]
            a[pivot] = a[col]
            a[col] = tmp
            for (row in col + 1 until n) {
                val factor = a[row][col] / a[col][col]
                for (j in col..n) {
                    a[row][j] -= factor * a[col][j]
                }
            }
        }
        val step = DoubleArray(n)
        for (row in n - 1 downTo 0) {
            val sum = (row + 1 until n).sumOf { a[row][it] * step[it] }
            step[row] = (a[row][n] - sum) / a[row][row]
        }
        return step.takeIf { values -> values.all { it.isFinite() } }
    }

    private class Candidate(val guess: List<Float>, val residuals: DoubleArray, val cost: Double, val stepSize: Double)

    private fun evaluate(
        guess: List<Float>,
        step: DoubleArray,
        residualsFn: (List<Float>) -> DoubleArray
    ): Candidate {
        val candidate = guess.mapIndexed { j, value -> value + step[j].toFloat() }
        val residuals = residualsFn(candidate)
        return Candidate(candidate, residuals, residuals.sumOf { it * it }, sqrt(step.sumOf { it * it }))
    }

    private fun limit(step: DoubleArray, maxAllowedStep: Float, dampingFactor: Float): DoubleArray {
        val largest = step.maxOf { abs(it) }
        val scale = if (largest > maxAllowedStep) maxAllowedStep / largest else 1.0
        return DoubleArray(step.size) { step[it] * scale * dampingFactor }
    }

    private companion object {
        const val INITIAL_DAMPING = 1e-3
        const val DAMPING_SCALE = 10.0
        const val MAX_DAMPING = 1e8
    }
}
