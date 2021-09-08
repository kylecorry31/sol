package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class HillClimbingOptimizerTest {

    @Test
    fun optimize() {
        val optimizer = HillClimbingOptimizer(0.01, 10000)
        val fn = { x: Double, y: Double ->
            x * x + y * y
        }

        val minimum = optimizer.optimize(Range(-10.0, 10.0), Range(-10.0, 10.0), maximize = false, fn = fn)

        assertEquals(0.0, minimum.first, 0.01)
        assertEquals(0.0, minimum.second, 0.01)
    }
}