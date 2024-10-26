package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.algebra.createMatrix
import com.kylecorry.sol.math.algebra.diagonalMatrix
import com.kylecorry.sol.math.algebra.dot
import com.kylecorry.sol.math.algebra.transpose
import com.kylecorry.sol.math.calculus.Calculus

internal object Trilateration {

    // TODO: Calculate x and y ranges from measurements
    fun trilaterate(
        measurements: List<Pair<Pair<Double, Double>, Range<Double>>>,
        distanceFn: (Pair<Double, Double>, Pair<Double, Double>) -> Double
    ): Pair<Double, Double> {

        val objectiveFn = { x: Double, y: Double, args: List<Pair<Pair<Double, Double>, Range<Double>>> ->
            var totalError = 0.0
            for (measurement in args) {
                val distance = distanceFn(x to y, measurement.first)
                totalError += when {
                    distance < measurement.second.start -> square(measurement.second.start - distance)
                    distance > measurement.second.end -> square(distance - measurement.second.end)
                    else -> 0.0
                }
            }
            totalError
        }

        val initialX = measurements.map { it.first.first }.average()
        val initialY = measurements.map { it.first.second }.average()

        return bfgsMinimize(
            objectiveFn,
            { x, y, args -> Calculus.derivative(x, y) { it1, it2 -> objectiveFn(it1, it2, args) } },
            initialX,
            initialY,
            measurements
        )
    }

    // Calculus.derivative(x, y){ it1, it2 -> objectiveFn(it1, it2, args) }

    private fun <T> bfgsMinimize(
        objectiveFn: (Double, Double, List<T>) -> Double,
        gradFn: (Double, Double, List<T>) -> Pair<Double, Double>,
        x0: Double,
        y0: Double,
        args: List<T>
    ): Pair<Double, Double> {
        // The BFGS algorithm
        // https://en.wikipedia.org/wiki/Broyden%E2%80%93Fletcher%E2%80%93Goldfarb%E2%80%93Shanno_algorithm

        val maxIterations = 1000
        val tolerance = 1e-6

        var x = x0
        var y = y0
        var H = diagonalMatrix(1f, 1f)
        var grad = gradFn(x, y, args)
        var p = doubleArrayOf(-grad.first, -grad.second)
        var alpha = 0.3
        var beta = 0.5
        var i = 0

        while (i < maxIterations && grad.first > tolerance && grad.second > tolerance) {
            val s = lineSearch(objectiveFn, gradFn, x, y, p, args, alpha, beta)
            x += s * p[0]
            y += s * p[1]
            val gradNew = gradFn(x, y, args)
            val yk = doubleArrayOf(gradNew.first - grad.first, gradNew.second - grad.second)
            val sk = doubleArrayOf(x - x0, y - y0)
            val rho = 1 / (yk[0] * sk[0] + yk[1] * sk[1])
            val A = createMatrix(2, 2, 0f)
            A[0][0] = (1 - rho * sk[0] * yk[0]).toFloat()
            A[0][1] = (-rho * sk[0] * yk[1]).toFloat()
            A[1][0] = (-rho * sk[1] * yk[0]).toFloat()
            A[1][1] = (1 - rho * sk[1] * yk[1]).toFloat()
            H = A.dot(H.dot(A.transpose()))
            grad = gradNew
            p = doubleArrayOf(-grad.first, -grad.second)
            i++
        }

        return x to y
    }

    private fun <T> lineSearch(
        objectiveFn: (Double, Double, List<T>) -> Double,
        gradFn: (Double, Double, List<T>) -> Pair<Double, Double>,
        x: Double,
        y: Double,
        p: DoubleArray,
        args: List<T>,
        alpha: Double,
        beta: Double
    ): Double {
        var s = 1.0
        var f = objectiveFn(x + s * p[0], y + s * p[1], args)
        var grad = gradFn(x + s * p[0], y + s * p[1], args)
        while (f > objectiveFn(x, y, args) + alpha * s * (grad.first * p[0] + grad.second * p[1])) {
            s *= beta
            f = objectiveFn(x + s * p[0], y + s * p[1], args)
            grad = gradFn(x + s * p[0], y + s * p[1], args)
        }
        return s
    }

}