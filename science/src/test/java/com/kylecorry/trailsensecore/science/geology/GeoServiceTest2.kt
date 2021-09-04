package com.kylecorry.trailsensecore.science.geology

import com.kylecorry.trailsensecore.units.Distance
import com.kylecorry.trailsensecore.units.DistanceUnits
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GeoServiceTest2 {

    @ParameterizedTest
    @MethodSource("provideAvalancheRisk")
    fun getAvalancheRisk(angle: Float, expected: AvalancheRisk) {
        val service = GeologyService()
        val risk = service.getAvalancheRisk(angle)
        assertEquals(expected, risk)
    }

    @Test
    fun getMapDistanceVerbal() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val scaleFrom = Distance(2f, DistanceUnits.Centimeters)
        val scaleTo = Distance(0.5f, DistanceUnits.Kilometers)

        val expected = Distance(0.635f, DistanceUnits.Kilometers)

        val service = GeologyService()
        val actual = service.getMapDistance(measurement, scaleFrom, scaleTo)

        assertEquals(expected.distance, actual.distance, 0.001f)
        assertEquals(expected.units, actual.units)
    }

    @Test
    fun getMapDistanceRatio() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val ratioFrom = 0.5f
        val ratioTo = 1.25f

        val expected = Distance(2.5f, DistanceUnits.Inches)

        val service = GeologyService()
        val actual = service.getMapDistance(measurement, ratioFrom, ratioTo)

        assertEquals(expected.distance, actual.distance, 0.001f)
        assertEquals(expected.units, actual.units)
    }

    companion object {
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