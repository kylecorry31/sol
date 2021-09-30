package com.kylecorry.sol.math.statistics

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class StatisticsServiceTest {

    private val service = StatisticsService()

    @ParameterizedTest
    @MethodSource("provideWeightedSum")
    fun weightedSum(values: List<Pair<Float, Float>>, expected: Float) {
        val actual = service.weightedMean(values)
        assertEquals(expected, actual, 0.00001f)
    }

    companion object {
        @JvmStatic
        fun provideWeightedSum(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    listOf<Pair<Float, Float>>(),
                    0f
                ),
                Arguments.of(
                    listOf(2f to 1f),
                    2f
                ),
                Arguments.of(
                    listOf(2f to 1f, 3f to 0f),
                    2f
                ),
                Arguments.of(
                    listOf(2f to 0.5f, 3f to 0.5f),
                    2.5f
                ),
                Arguments.of(
                    listOf(0.9f to 0.1f, 0.5f to 0.9f),
                    0.54f
                )
            )
        }
    }
}