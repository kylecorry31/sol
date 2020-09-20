package com.kylecorry.trailsensecore.domain.inclinometer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class InclinationServiceTest {

    private val inclinationService = InclinationService()

    @ParameterizedTest
    @MethodSource("provideAvalancheRisk")
    fun getAvalancheRisk(angle: Float, expected: AvalancheRisk) {
        val risk = inclinationService.getAvalancheRisk(angle)
        assertEquals(expected, risk)
    }

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

        @JvmStatic
        fun provideAvalancheRisk(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, AvalancheRisk.Low),
                Arguments.of(19f, AvalancheRisk.Low),
                Arguments.of(-19f, AvalancheRisk.Low),
                Arguments.of(20f, AvalancheRisk.Moderate),
                Arguments.of(29f, AvalancheRisk.Moderate),
                Arguments.of(51f, AvalancheRisk.Moderate),
                Arguments.of(90f, AvalancheRisk.Moderate),
                Arguments.of(-51f, AvalancheRisk.Moderate),
                Arguments.of(30f, AvalancheRisk.High),
                Arguments.of(45f, AvalancheRisk.High),
                Arguments.of(50f, AvalancheRisk.High),
                Arguments.of(-45f, AvalancheRisk.High),
            )
        }
    }
}