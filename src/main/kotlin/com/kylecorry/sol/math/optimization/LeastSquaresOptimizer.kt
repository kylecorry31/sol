package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.algebra.dot
import com.kylecorry.sol.math.algebra.inverse
import com.kylecorry.sol.math.algebra.transpose
import com.kylecorry.sol.math.geometry.Geometry
import kotlin.math.abs

class LeastSquaresOptimizer {
    fun optimize(
        points: List<List<Float>>,
        errors: List<Float>,
        maxIterations: Int = 100,
        maxAllowedStep: Float = 500f,
        dampingFactor: Float = 0.1f,
        tolerance: Float = 0.0001f,
        distanceFn: (List<Float>, List<Float>) -> Float = { a, b -> Geometry.euclideanDistance(a, b) },
        weightingFn: (index: Int, point: List<Float>, error: Float) -> Float = { index, point, error -> 1f }
    ): List<Float> {
        if (points.size < 2) {
            return points.firstOrNull() ?: emptyList()
        }

        require(points.size == errors.size) { "The number of points and errors must be equal." }

        // Initial guess for the observer location (average of the points)
        val guess = points[0].mapIndexed { index, _ ->
            points.map { it[index] }.average().toFloat()
        }.toMutableList()

        var lastError = Float.MAX_VALUE
        var averageError = 0f
        for (i in 0 until maxIterations) {

            val f = points.mapIndexed { i, point ->
                (distanceFn(point, guess) - errors[i]) * weightingFn(i, point, errors[i])
            }.toTypedArray()

            val jacobian = points.mapIndexed { i, point ->
                val distance = distanceFn(point, guess)
                point.mapIndexed { j, value -> ((guess[j] - value) / distance) * weightingFn(i, point, errors[i]) }
                    .toTypedArray()
            }.toTypedArray()

            val jacobianT = jacobian.transpose()

            val jacobianTJacobian = jacobianT.dot(jacobian)

            val jacobianTF = jacobianT.dot(arrayOf(f).transpose())

            val step = jacobianTJacobian.inverse().dot(jacobianTF)

            if ((step.maxOfOrNull { abs(it[0]) } ?: 0f) > maxAllowedStep) {
                val maxStep = step.maxOfOrNull { abs(it[0]) } ?: 0f
                step.forEachIndexed { index, it ->
                    step[index][0] = it[0] / maxStep * maxAllowedStep
                }
            }

            guess.forEachIndexed { index, value ->
                guess[index] -= step[index][0] * dampingFactor
            }

            val error = f.map { abs(it) }.sum()
            averageError = f.map { abs(it) }.average().toFloat()
            if (abs(lastError - error) < tolerance) {
                lastError = error
                break
            }
            lastError = error
        }

        return guess
    }

}