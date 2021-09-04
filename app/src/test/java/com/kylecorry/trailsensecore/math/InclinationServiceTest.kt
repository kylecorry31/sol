package com.kylecorry.trailsensecore.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class InclinationServiceTest {

    private val inclinationService = InclinationService()

    @ParameterizedTest
    @MethodSource("provideHeights")
    fun calculateHeight(angle: Float, distance: Float, phoneHeight: Float, expected: Float) {
        val height = inclinationService.estimateHeight(distance, angle, phoneHeight)
        assertEquals(expected, height, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideHeightAngles")
    fun calculateHeightAngles(distance: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val height = inclinationService.estimateHeightAngles(distance, downAngle, upAngle)
        assertEquals(expected, height, 0.01f)
    }


    companion object {

        @JvmStatic
        fun provideHeights(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(35f, 15.6f, 0f, 10.92f),
                Arguments.of(35f, 15.6f, 1.64f, 12.56f),
                Arguments.of(0f, 15.6f, 1.64f, 1.64f),
                Arguments.of(90f, 15.6f, 1.64f, 0f),
                Arguments.of(-35f, 15.6f, 1.64f, 0f),
            )
        }

        @JvmStatic
        fun provideHeightAngles(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(10f, 45f, 0f, 10f),
                Arguments.of(10f, 20f, 0f, 3.6397f),

                Arguments.of(10f, 45f, -5f, 10.8748f),
                Arguments.of(10f, 20f, 5f, 2.764814f),

                Arguments.of(10f, -5f, -20f, 2.764814f),

                Arguments.of(10f, 5f, 20f, 0f),
                Arguments.of(10f, 90f, 0f, Float.POSITIVE_INFINITY),
                Arguments.of(10f, 0f, 90f, Float.POSITIVE_INFINITY),
                Arguments.of(10f, -90f, 0f, Float.POSITIVE_INFINITY),
                Arguments.of(10f, 0f, -90f, Float.POSITIVE_INFINITY),
            )
        }
    }
}