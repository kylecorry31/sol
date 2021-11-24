package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import org.junit.Assert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GeologyServiceTest {

    @Test
    fun triangulate() {
        val service = GeologyService()
        val pointA = Coordinate(40.0, 10.0)
        val bearingA = Bearing(220f)
        val pointB = Coordinate(40.5, 9.5)
        val bearingB = Bearing(295f)

        val expected = Coordinate(40.229722, 10.252778)
        val actual = service.triangulate(pointA, bearingA, pointB, bearingB)

        Assert.assertNotNull(actual)
        Assert.assertEquals(expected.latitude, actual!!.latitude, 0.01)
        Assert.assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @Test
    fun deadReckon() {
        val service = GeologyService()
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(280f)
        val distance = 10000f

        val expected = Coordinate(39.984444, 10.115556)
        val actual = service.deadReckon(start, distance, bearing)
        Assert.assertEquals(expected.latitude, actual.latitude, 0.01)
        Assert.assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @Test
    fun navigate() {
        val service = GeologyService()
        val start = Coordinate(0.0, 1.0)
        val end = Coordinate(10.0, -8.0)

        val vector = service.navigate(start, end, 0f, true)

        Assert.assertEquals(Bearing(-41.7683f).value, vector.direction.value, 0.005f)
        Assert.assertEquals(1488793.6f, vector.distance, 0.005f)
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

    @ParameterizedTest
    @MethodSource("provideCrossTrack")
    fun crossTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate, expected: Float) {
        val service = GeologyService()

        val actual = service.getCrossTrackDistance(point, start, end)

        assertEquals(expected, actual.meters().distance, 1f)
    }

    companion object {

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