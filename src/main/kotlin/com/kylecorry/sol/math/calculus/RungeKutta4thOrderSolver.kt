package com.kylecorry.sol.math.calculus

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector

class RungeKutta4thOrderSolver : ODESolver {

    override fun solve(
        x: Range<Float>,
        stepSize: Float,
        initialY: Vector,
        derivative: (x: Float, y: Vector) -> Vector
    ): List<Pair<Float, Vector>> {
        // https://www.geeksforgeeks.org/dsa/runge-kutta-4th-order-method-solve-differential-equation/
        val results = mutableListOf<Pair<Float, Vector>>()
        var y = initialY
        var currentX = x.start

        while (currentX < x.end) {
            results.add(Pair(currentX, y))

            val k1 = derivative(currentX, y) * stepSize
            val k2 = derivative(currentX + stepSize / 2f, y + k1 * 0.5f) * stepSize
            val k3 = derivative(currentX + stepSize / 2f, y + k2 * 0.5f) * stepSize
            val k4 = derivative(currentX + stepSize, y + k3) * stepSize

            y += (k1 + k2 * 2f + k3 * 2f + k4) * (1 / 6f)
            currentX += stepSize
        }

        return results
    }
}