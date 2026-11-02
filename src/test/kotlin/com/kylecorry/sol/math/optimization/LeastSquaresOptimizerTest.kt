package com.kylecorry.sol.math.optimization

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.atan

class LeastSquaresOptimizerTest {

    @Test
    fun convergesWhereGaussNewtonOvershoots() {
        // Full Gauss-Newton steps on atan(x) = 0 diverge when starting at |x| > 1.39
        val result = LeastSquaresOptimizer().optimize(
            listOf(listOf(0f), listOf(0f)),
            listOf(0f, 0f),
            dampingFactor = 1f,
            initialValue = listOf(2f),
            distanceFn = { _, guess -> atan(guess[0]) },
            jacobianFn = { _, _, guess -> listOf(1 / (1 + guess[0] * guess[0])) }
        )
        assertEquals(0f, result[0], 1e-3f)
    }
}
