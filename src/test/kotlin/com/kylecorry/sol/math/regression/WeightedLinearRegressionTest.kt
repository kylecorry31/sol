package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WeightedLinearRegressionTest {

    @Test
    fun requiresNonZeroWeights() {
        assertThrows<IllegalArgumentException> {
            WeightedLinearRegression(
                listOf(
                    Vector2(0f, 1f),
                    Vector2(1f, 3f),
                    Vector2(2f, 5f)
                ),
                listOf(0f, 0f, 0f)
            )
        }
    }
}
