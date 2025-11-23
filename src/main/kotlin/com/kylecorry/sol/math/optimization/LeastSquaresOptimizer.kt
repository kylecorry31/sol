package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Vector
import com.kylecorry.sol.math.algebra.LinearAlgebra
import com.kylecorry.sol.math.algebra.Matrix
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
            return points.firstOrNull() ?: emptyList()
        }

        require(points.size == errors.size) { "The number of points and errors must be equal." }

        // Initial guess for the observer location (average of the points)
        val guess = initialValue?.toMutableList() ?: points[0].mapIndexed { index, _ ->
            points.map { it[index] }.average().toFloat()
        }.toMutableList()

        for (i in 0 until maxIterations) {

            val f = Vector(points.mapIndexed { i, point ->
                (errors[i] - distanceFn(point, guess)) * weightingFn(i, point, errors[i])
            }.toFloatArray())

            val jacobian = Matrix.create(points.mapIndexed { i, point ->
                jacobianFn(i, point, guess).toTypedArray()
            }.toTypedArray())

            val step = LinearAlgebra.leastSquares(jacobian, f)

            if ((step.data.maxOfOrNull { abs(it) } ?: 0f) > maxAllowedStep) {
                val maxStep = step.data.maxOfOrNull { abs(it) } ?: 0f
                step.data.forEachIndexed { index, it ->
                    step[index] = it / maxStep * maxAllowedStep
                }
            }

            guess.forEachIndexed { index, value ->
                guess[index] += step[index] * dampingFactor
            }

            val delta = step.toColumnMatrix().norm()
            if (delta < tolerance) {
                break
            }
        }

        return guess
    }

}