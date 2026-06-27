package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.math.algebra.Matrix
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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

    @ParameterizedTest
    @MethodSource("provideRange")
    fun range(values: List<Float>, expected: Float) {
        val actual = Statistics.range(values)

        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideInterquartileRange")
    fun interquartileRange(values: List<Float>, interpolate: Boolean, expected: Float) {
        val actual = Statistics.interquartileRange(values, interpolate)

        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideMedianAbsoluteDeviation")
    fun medianAbsoluteDeviation(values: List<Float>, median: Float?, expected: Float) {
        val actual = Statistics.medianAbsoluteDeviation(values, median)

        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0",
        "1, 1.4826",
        "2.5, 3.7065"
    )
    fun medianAbsoluteDeviationToStandardDeviation(
        medianAbsoluteDeviation: Float,
        expected: Float
    ) {
        val actual = Statistics.medianAbsoluteDeviationToStandardDeviation(medianAbsoluteDeviation)

        assertEquals(expected, actual, 0.00001f)
    }

    @Test
    fun textureFeatures() {
        val glcm4x4 = arrayOf(
            floatArrayOf(0.3333f, 0.0833f, 0.0833f, 0f),
            floatArrayOf(0.0833f, 0f, 0.0833f, 0.0833f),
            floatArrayOf(0.0833f, 0.0833f, 0f, 0f),
            floatArrayOf(0f, 0.0833f, 0f, 0f)
        )
        val features = Statistics.textureFeatures(Matrix.create(glcm4x4))
        assertEquals(2.02f, features.entropy, 0.1f)
        assertEquals(0.41f, features.energy, 0.1f)
        assertEquals(1.66f, features.contrast, 0.1f)
        assertEquals(0.57f, features.homogeneity, 0.1f)
        assertEquals(0.99f, features.dissimilarity, 0.1f)
        assertEquals(0.17f, features.angularSecondMoment, 0.1f)
        assertEquals(0.833f, features.horizontalMean, 0.1f)
        assertEquals(0.833f, features.verticalMean, 0.1f)
        assertEquals(0.97f, features.horizontalVariance, 0.1f)
        assertEquals(0.97f, features.verticalVariance, 0.1f)
        assertEquals(0.14f, features.correlation, 0.1f)
        assertEquals(0.33f, features.max, 0.1f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 2, 0",
        "1, 2, 0.5",
        "-1, 2, 0.5",
        "2, 2, 2",
        "-2, 2, 2",
        "3, 2, 4",
        "-3, 2, 4"
    )
    fun huberLoss(residual: Float, threshold: Float, expected: Float) {
        val actual = Statistics.huberLoss(residual, threshold)

        assertEquals(expected, actual, 0.00001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 2, 1",
        "1, 2, 1",
        "-1, 2, 1",
        "2, 2, 1",
        "-2, 2, 1",
        "4, 2, 0.5",
        "-4, 2, 0.5"
    )
    fun huberWeight(residual: Float, threshold: Float, expected: Float) {
        val actual = Statistics.huberWeight(residual, threshold)

        assertEquals(expected, actual, 0.00001f)
    }

    companion object {

        @JvmStatic
        fun provideMedianAbsoluteDeviation(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Float>(), null, 0f),
                Arguments.of(listOf(5f), null, 0f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), null, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), null, 1f),
                Arguments.of(listOf(5f, 1f, 3f, 2f, 4f), null, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 100f), null, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 4f, 1f),
                Arguments.of(listOf(-2f, -1f, 0f, 1f, 2f), null, 1f)
            )
        }

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
        fun provideRange(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Float>(), 0f),
                Arguments.of(listOf(5f), 0f),
                Arguments.of(listOf(1f, 2f), 1f),
                Arguments.of(listOf(5f, 1f, 3f, 2f, 4f), 4f),
                Arguments.of(listOf(-3f, -1f, -7f), 6f),
                Arguments.of(listOf(-2f, 4f, 0f), 6f),
                Arguments.of(listOf(2f, 2f, 2f), 0f)
            )
        }

        @JvmStatic
        fun provideInterquartileRange(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Float>(), true, 0f),
                Arguments.of(listOf(5f), true, 0f),
                Arguments.of(listOf(1f, 2f), true, 0.5f),
                Arguments.of(listOf(1f, 2f), false, 1f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), true, 2f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), false, 2f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), true, 1.5f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), false, 1f),
                Arguments.of(listOf(5f, 1f, 3f, 2f, 4f), true, 2f),
                Arguments.of(listOf(-3f, -1f, -7f, 2f), true, 3.75f),
                Arguments.of(listOf(2f, 2f, 2f, 2f), true, 0f)
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
