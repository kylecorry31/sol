package com.kylecorry.sol.math.statistics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GaussianDistributionTest {

    @ParameterizedTest
    @CsvSource(
        "0, 1, 1",
        "0, 2, 4",
        "1, 1, 1",
        "1, 2, 4",
        "0, 4, 16"
    )
    fun getVariance(mean: Float, standardDeviation: Float, expected: Float) {
        val distribution = GaussianDistribution(mean, standardDeviation)
        assertEquals(expected, distribution.variance)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1, 0, 0.3989423",
        "0, 1, 1, 0.2419707",
        "0, 1, 2, 0.05399097",
        "0, 1, 3, 0.004431848",
        "0, 1, 4, 0.0001338302",
        "0, 1, 5, 1.48672E-06",
        "1, 1, 0, 0.2419707",
        "1, 1, 1, 0.3989423",
        "1, 1, 2, 0.2419707",
    )
    fun probability(mean: Float, standardDeviation: Float, x: Float, expected: Float) {
        val distribution = GaussianDistribution(mean, standardDeviation)
        assertEquals(expected, distribution.probability(x), 0.0001f)
    }
}