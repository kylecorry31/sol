package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class InclinationServiceTest {

    private val inclinationService = InclinationService()

    @ParameterizedTest
    @MethodSource("provideHeights")
    fun calculateHeights(distance: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val height = inclinationService.height(distance, downAngle, upAngle)
        assertEquals(expected, height, 0.01f)
    }

    @Test
    fun calculateDistance(){
        val d = inclinationService.distance(15f, 0f, 4.57392126f)
        assertEquals(187.5f, d, 0.5f)

        val d2 = inclinationService.distance(15f, -1.0f, 3.57392126f)
        assertEquals(187.5f, d2, 0.5f)
    }


    companion object {

        @JvmStatic
        fun provideHeights(): Stream<Arguments> {
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