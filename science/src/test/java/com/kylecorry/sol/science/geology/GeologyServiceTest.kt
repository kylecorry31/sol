package com.kylecorry.sol.science.geology

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.*
import org.junit.Assert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GeologyServiceTest {

    private val service = GeologyService()

    @Test
    fun gravity() {
        val service = GeologyService()
        assertThat(service.getGravity(Coordinate.zero)).isCloseTo(9.78032677f, 0.00001f)
        assertThat(service.getGravity(Coordinate(90.0, 0.0))).isCloseTo(
            9.83218493786340046183f,
            0.00001f
        )
        assertThat(service.getGravity(Coordinate(-90.0, 0.0))).isCloseTo(
            9.83218493786340046183f,
            0.00001f
        )
    }

    @Test
    fun triangulate() {
        val service = GeologyService()
        val pointA = Coordinate(40.0, 10.0)
        val bearingA = Bearing(220f)
        val pointB = Coordinate(40.5, 9.5)
        val bearingB = Bearing(295f)

        val expected = Coordinate(40.229722, 10.252778)
        val actual = service.triangulate(pointA, bearingA, pointB, bearingB)

        assertThat(actual).isNotNull().isCloseTo(expected, 20f)
    }

    @Test
    fun deadReckon() {
        val service = GeologyService()
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(280f)
        val distance = 10000f

        val expected = Coordinate(39.984444, 10.115556)
        val actual = service.deadReckon(start, distance, bearing)

        assertThat(actual).isCloseTo(expected, 20f)
    }

    @Test
    fun navigate() {
        val service = GeologyService()
        val start = Coordinate(0.0, 1.0)
        val end = Coordinate(10.0, -8.0)

        val vector = service.navigate(start, end, 0f, true)

        assertThat(vector.direction.value).isCloseTo(Bearing(-41.7683f).value, 0.005f)
        assertThat(vector.distance).isCloseTo(1488793.6f, 0.005f)
    }

    @Test
    fun getDeclination() {
        val service = GeologyService()
        val ny = Coordinate(40.7128, -74.0060)
        val altitude = 10f
        val dec = service.getGeomagneticDeclination(ny, altitude, 1608151299005)
        Assert.assertEquals(-12.708426f, dec, 0.01f)
    }

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

        assertThat(actual).isCloseTo(expected, 0.001f)
    }

    @Test
    fun getMapDistanceRatio() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val ratioFrom = 0.5f
        val ratioTo = 1.25f

        val expected = Distance(2.5f, DistanceUnits.Inches)

        val service = GeologyService()
        val actual = service.getMapDistance(measurement, ratioFrom, ratioTo)

        assertThat(actual).isCloseTo(expected, 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideCrossTrack")
    fun crossTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate, expected: Float) {
        val service = GeologyService()

        val actual = service.getCrossTrackDistance(point, start, end)

        assertThat(actual).isCloseTo(Distance.meters(expected), 1f)
    }

    @Test
    fun elevationGainLoss() {
        val elevations = listOf(5f, 1f, 2f, 4f, 2f, 4f, 4f).map { Distance.meters(it) }
        val expectedGain = Distance.meters(5f)
        val expectedLoss = Distance.meters(-6f)
        val service = GeologyService()

        val gain = service.getElevationGain(elevations)
        val loss = service.getElevationLoss(elevations)

        assertThat(gain).isEqualTo(expectedGain)
        assertThat(loss).isEqualTo(expectedLoss)
    }

    @ParameterizedTest
    @MethodSource("provideHeights")
    fun calculateHeights(distance: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val height = service.getHeightFromInclination(Distance.meters(distance), downAngle, upAngle)
        assertEquals(expected, height.distance, 0.01f)
    }

    @Test
    fun calculateDistance() {
        val d = service.getDistanceFromInclination(Distance.meters(15f), 0f, 4.57392126f)
        assertEquals(187.5f, d.distance, 0.5f)

        val d2 = service.getDistanceFromInclination(Distance.meters(15f), -1.0f, 3.57392126f)
        assertEquals(187.5f, d2.distance, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideInclination")
    fun getInclination(angle: Float, expected: Float){
        val inclination = service.getInclination(angle)
        assertEquals(expected, inclination, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideGrade")
    fun getSlopeGrade(angle: Float, expected: Float){
        val grade = service.getSlopeGrade(angle)
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
        fun provideCrossTrack(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Coordinate(80.0, 80.0),
                    Coordinate(0.0, 0.0),
                    Coordinate(90.0, 90.0),
                    1094921f
                ),
                Arguments.of(
                    Coordinate(1.5, 2.5),
                    Coordinate(1.2, 0.8),
                    Coordinate(1.0, 2.0),
                    -63994.76f
                ),
                Arguments.of(
                    Coordinate(40.0, 180.0),
                    Coordinate(30.0, 100.0),
                    Coordinate(50.0, 210.0),
                    1893409f
                ),
                Arguments.of(
                    Coordinate(0.0, 0.02),
                    Coordinate(0.0, 0.0),
                    Coordinate(0.0, 0.01),
                    0f
                ),
                Arguments.of(
                    Coordinate(0.001, 0.0),
                    Coordinate(0.0, 0.0),
                    Coordinate(0.0, 0.001),
                    -111.1984f
                ),
                Arguments.of(
                    Coordinate(0.001, 0.0),
                    Coordinate(0.0, 0.0),
                    Coordinate(0.0, -0.001),
                    111.1984f
                ),
                Arguments.of(
                    Coordinate(-0.001, 0.0),
                    Coordinate(0.0, 0.0),
                    Coordinate(0.0, 0.001),
                    111.1984f
                )
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