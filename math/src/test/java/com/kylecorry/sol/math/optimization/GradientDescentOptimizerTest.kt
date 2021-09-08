package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class GradientDescentOptimizerTest {

    @Test
    fun optimize() {
        val fn = { x: Double, y: Double ->
            x * x + y * y
        }
        val optimizer = GradientDescentOptimizer(
            0.1,
            10000,
        ) { x: Double, y: Double ->
            val xGrad = 2 * x
            val yGrad = 2 * y
            xGrad to yGrad
        }
        val minimum =
            optimizer.optimize(Range(-10.0, 10.0), Range(-10.0, 10.0), maximize = false, fn = fn)

        assertEquals(0.0, minimum.first, 0.001)
        assertEquals(0.0, minimum.second, 0.001)
    }

    @Test
    fun optimizeWithApproximateGrad() {
        val fn = { x: Double, y: Double ->
            x * x + y * y
        }
        val optimizer = GradientDescentOptimizer(
            0.1,
            1000,
            gradientFn = GradientDescentOptimizer.approximateGradientFn(fn = fn)
        )
        val minimum =
            optimizer.optimize(Range(-10.0, 10.0), Range(-10.0, 10.0), maximize = false, fn = fn)

        assertEquals(0.0, minimum.first, 0.001)
        assertEquals(0.0, minimum.second, 0.001)
    }
}