package com.kylecorry.sol.math.optimization

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.math.sqrt

internal class OptimizationTest {
    @ParameterizedTest
    @CsvSource(
        "2",
        "9",
    )
    fun newtonRaphson(value: Double) {
        val result =
            Optimization.newtonRaphsonIteration(initialValue = 1.0) { x ->
                (x + value / x) / 2.0
            }
        assertEquals(sqrt(value), result, 1e-6)
    }

    @ParameterizedTest
    @CsvSource(
        "2",
        "9",
    )
    fun newtonRaphsonFloat(value: Float) {
        val result =
            Optimization.newtonRaphsonIteration(initialValue = 1f) { x ->
                (x + value / x) / 2f
            }
        assertEquals(sqrt(value), result, 1e-6f)
    }

    @Test
    fun newtonRaphsonRespectsMaxIterations() {
        var callCount = 0
        Optimization.newtonRaphsonIteration(initialValue = 1.0, maxIterations = 5) { x ->
            callCount++
            (x + 2.0 / x) / 2.0
        }
        assertEquals(5, callCount)
    }

    @Test
    fun newtonRaphsonRespectsTolerance() {
        var callCount = 0
        val result =
            Optimization.newtonRaphsonIteration(initialValue = 1.0, maxIterations = 5, tolerance = 1.0) { x ->
                callCount++
                (x + 2.0 / x) / 2.0
            }
        assertEquals(1, callCount)
        assertEquals(sqrt(2.0), result, 1.0)
    }
}
