package com.kylecorry.sol.math.statistics

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class StatisticsTest {
    
    @ParameterizedTest
    @MethodSource("provideWeightedSum")
    fun weightedSum(values: List<Pair<Float, Float>>, expected: Float) {
        val actual = Statistics.weightedMean(values)
        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideProbability")
    fun probability(values: List<Float>, expected: List<Float>){
        val actual = Statistics.probability(values)
        for (i in values.indices){
            assertEquals(expected[i], actual[i], 0.00001f)
        }
    }

    @ParameterizedTest
    @MethodSource("provideSoftmax")
    fun softmax(values: List<Float>, expected: List<Float>){
        val actual = Statistics.softmax(values)
        for (i in values.indices){
            assertEquals(expected[i], actual[i], 0.001f)
        }
    }

    @ParameterizedTest
    @MethodSource("provideQuantile")
    fun quantile(values: List<Float>, quantile: Float, interpolate: Boolean, expected: Float) {
        val actual = Statistics.quantile(values, quantile, interpolate)
        assertEquals(expected, actual, 0.00001f)
    }

    companion object {

        @JvmStatic
        fun provideQuantile(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Float>(), 0.5f, true, 0f),

                // With odd values, interpolate
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.5f, true, 3f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.25f, true, 2f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.75f, true, 4f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.0f, true, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 1f, true, 5f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.1f, true, 1.4f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.9f, true, 4.6f),

                // With odd values, don't interpolate
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.5f, false, 3f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.25f, false, 2f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.75f, false, 4f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.0f, false, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 1f, false, 5f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.1f, false, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 0.9f, false, 5f),

                // With even values, interpolate
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.5f, true, 2.5f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.25f, true, 1.75f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.75f, true, 3.25f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.0f, true, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 1f, true, 4f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.1f, true, 1.3f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.9f, true, 3.7f),

                // With even values, don't interpolate
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.5f, false, 2f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.25f, false, 2f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.75f, false, 3f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.0f, false, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 1f, false, 4f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.1f, false, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 0.9f, false, 4f),

            )
        }

        @JvmStatic
        fun provideSoftmax(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    listOf<Float>(),
                    listOf<Float>()
                ),
                Arguments.of(
                    listOf(1f, 0f, 0f),
                    listOf(0.576f, 0.212f, 0.212f)
                ),
                Arguments.of(
                    listOf(1f, 3f, 6f),
                    listOf(0.006f, 0.047f, 0.946f)
                ),
            )
        }

        @JvmStatic
        fun provideProbability(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    listOf<Float>(),
                    listOf<Float>()
                ),
                Arguments.of(
                    listOf(1f, 0f, 0f),
                    listOf(1f, 0f, 0f)
                ),
                Arguments.of(
                    listOf(1f, 3f, 6f),
                    listOf(0.1f, 0.3f, 0.6f)
                ),
            )
        }

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