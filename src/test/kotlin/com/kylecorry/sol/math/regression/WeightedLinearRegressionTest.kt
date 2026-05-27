package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun hasBadFitWhenSinglePoint() {
        val regression = WeightedLinearRegression(
            listOf(Vector2(1f, 2f)),
            listOf(1f)
        )

        assertTrue(regression.hasBadFit)
    }

    @Test
    fun hasBadFitWhenXVarianceIsBelowAccuracy() {
        val regression = WeightedLinearRegression(
            listOf(
                Vector2(1f, 2f),
                Vector2(1.01f, 3f)
            ),
            listOf(1f, 1f),
            accuracy = 0.1f
        )

        assertTrue(regression.hasBadFit)
    }

    @Test
    fun doesNotHaveBadFitWhenXVarianceMeetsAccuracy() {
        val regression = WeightedLinearRegression(
            listOf(
                Vector2(0f, 1f),
                Vector2(1f, 3f),
                Vector2(2f, 5f)
            ),
            listOf(1f, 1f, 1f),
            accuracy = 0.1f
        )

        assertFalse(regression.hasBadFit)
    }
}
