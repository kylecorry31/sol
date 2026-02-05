package com.kylecorry.sol.science.geology

import assertk.assertThat
import assertk.assertions.isCloseTo
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GeologyTest {

    @ParameterizedTest
    @MethodSource("provideAvalancheRisk")
    fun getAvalancheRisk(angle: Float, expected: AvalancheRisk) {
        val risk = Geology.getAvalancheRisk(angle)
        assertEquals(expected, risk)
    }

    @ParameterizedTest
    @MethodSource("provideHeights")
    fun calculateHeights(distance: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val height = Geology.getHeightFromInclination(Distance.meters(distance), downAngle, upAngle)
        assertEquals(expected, height.value, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideDistances")
    fun calculateDistance(height: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val d = Geology.getDistanceFromInclination(Distance.meters(height), downAngle, upAngle)
        assertEquals(expected, d.value, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideInclination")
    fun getInclination(angle: Float, expected: Float) {
        val inclination = Geology.getInclination(angle)
        assertEquals(expected, inclination, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideGrade")
    fun getSlopeGrade(angle: Float, expected: Float) {
        val grade = Geology.getSlopeGrade(angle)
        assertEquals(expected, grade, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideGradeDistance")
    fun getSlopeGradeFromDistance(verticalMeters: Float, horizontalMeters: Float, expected: Float) {
        val vertical = Distance.meters(verticalMeters)
        val horizontal = Distance.meters(horizontalMeters)
        val grade = Geology.getSlopeGrade(vertical, horizontal)
        assertEquals(expected, grade, 0.01f)
    }

    companion object {

        @JvmStatic
        fun provideGrade(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(45f, 100f),
                Arguments.of(25f, 46.63f),
                Arguments.of(10f, 17.63f),
                Arguments.of(0f, 0f),
                Arguments.of(-45f, -100f),
                Arguments.of(-25f, -46.63f),
                Arguments.of(-10f, -17.63f),
                Arguments.of(50f, 119.18f),
            )
        }

        @JvmStatic
        fun provideGradeDistance(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1f, 1f, 100f),
                Arguments.of(20f, 1f, 5f),
                Arguments.of(20f, 1f, 5f),
                Arguments.of(0f, 10f, Float.POSITIVE_INFINITY),
                Arguments.of(0f, 0f, 0f),
                Arguments.of(0f, -10f, Float.NEGATIVE_INFINITY),
                Arguments.of(20f, -1f, -5f),
                Arguments.of(20f, -1f, -5f),
                Arguments.of(1f, -1f, -100f),
            )
        }


        @JvmStatic
        fun provideInclination(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(10f, 10f),
                Arguments.of(90f, 90f),
                Arguments.of(95f, 85f),
                Arguments.of(180f, 0f),
                Arguments.of(185f, -5f),
                Arguments.of(270f, -90f),
                Arguments.of(280f, -80f),
            )
        }

        @JvmStatic
        fun provideHeights(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(10f, 45f, 0f, 10f),
                Arguments.of(10f, 20f, 0f, 3.6397f),

                Arguments.of(10f, 45f, -5f, 10.8748f),
                Arguments.of(10f, 20f, 5f, 2.764814f),

                Arguments.of(10f, -5f, -20f, 2.764814f),

                Arguments.of(10f, 5f, 20f, 2.7648158f),
                Arguments.of(10f, 90f, 0f, Float.POSITIVE_INFINITY),
                Arguments.of(10f, 0f, 90f, Float.POSITIVE_INFINITY),
                Arguments.of(10f, -90f, 0f, Float.POSITIVE_INFINITY),
                Arguments.of(10f, 0f, -90f, Float.POSITIVE_INFINITY),
            )
        }

        @JvmStatic
        fun provideDistances(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(15f, 0f, 4.5739f, 187.5f),
                Arguments.of(10f, 45f, 0f, 10f),
                Arguments.of(3.6397f, 20f, 0f, 10f),

                Arguments.of(10.8748f, 45f, -5f, 10f),
                Arguments.of(2.764814f, 20f, 5f, 10f),

                Arguments.of(2.764814f, -5f, -20f, 10f),

                Arguments.of(2.7648158f, 5f, 20f, 10f),
                Arguments.of(10f, 90f, 0f, 0f),
                Arguments.of(10f, 0f, 90f, 0f),
                Arguments.of(10f, -90f, 0f, 0f),
                Arguments.of(10f, 0f, -90f, 0f),
            )
        }

        @JvmStatic
        fun provideAvalancheRisk(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, AvalancheRisk.Low),
                Arguments.of(19f, AvalancheRisk.Low),
                Arguments.of(-19f, AvalancheRisk.Low),
                Arguments.of(20f, AvalancheRisk.Low),
                Arguments.of(29f, AvalancheRisk.Low),
                Arguments.of(51f, AvalancheRisk.Moderate),
                Arguments.of(90f, AvalancheRisk.Low),
                Arguments.of(-51f, AvalancheRisk.Moderate),
                Arguments.of(30f, AvalancheRisk.High),
                Arguments.of(45f, AvalancheRisk.High),
                Arguments.of(50f, AvalancheRisk.Moderate),
                Arguments.of(-45f, AvalancheRisk.High),
            )
        }
    }
}