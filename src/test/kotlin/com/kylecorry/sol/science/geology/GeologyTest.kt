package com.kylecorry.sol.science.geology

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GeologyTest {

    @ParameterizedTest
    @MethodSource("provideAltitudes")
    fun altitude(pressure: Float, seaLevel: Float, altitude: Float) {
        val actual = Geology.getAltitude(Pressure.hpa(pressure), Pressure.hpa(seaLevel))
        val expected = Distance.meters(altitude)
        assertThat(actual).isCloseTo(expected, 1f)
    }

    @Test
    fun gravity() {
        assertThat(Geology.getGravity(Coordinate.zero)).isCloseTo(9.78032677f, 0.00001f)
        assertThat(Geology.getGravity(Coordinate(90.0, 0.0))).isCloseTo(
            9.83218493786340046183f,
            0.00001f
        )
        assertThat(Geology.getGravity(Coordinate(-90.0, 0.0))).isCloseTo(
            9.83218493786340046183f,
            0.00001f
        )
    }

    @Test
    fun triangulateSelf() {
        val pointA = Coordinate(40.0, 10.0)
        val bearingA = Bearing(220f)
        val pointB = Coordinate(40.5, 9.5)
        val bearingB = Bearing(295f)

        val expected = Coordinate(40.229722, 10.252778)
        val actual = Geology.triangulateSelf(pointA, bearingA, pointB, bearingB)

        assertThat(actual).isNotNull().isCloseTo(expected, 20f)
    }

    @ParameterizedTest
    @CsvSource(
        "41.94301,-71.78259,201.8,41.94177,-71.78014,228.9,41.93875,-71.78488",
        // Triangulate a different point with the same reference points
        "41.94301,-71.78259,72.2,41.94177,-71.78014,43.1,41.94433,-71.77705",
        // Swap calculated with one of the reference points
        "41.94301,-71.78259,123.7,41.94433,-71.77705,222.4,41.94177,-71.78014",
        // Order of reference points doesn't matter
        "41.94177,-71.78014,228.9,41.94301,-71.78259,201.8,41.93875,-71.78488",
        // Impossible to triangulate
        "41.94301,-71.78259,23.7,41.94433,-71.77705,222.4,,",
    )
    fun triangulateDestination(
        latA: Double,
        lonA: Double,
        degA: Float,
        latB: Double,
        lonB: Double,
        degB: Float,
        latExpected: Double?,
        lonExpected: Double?,
    ) {
        val pointA = Coordinate(latA, lonA)
        val bearingA = Bearing(degA)
        val pointB = Coordinate(latB, lonB)
        val bearingB = Bearing(degB)
        val expected = if (latExpected == null || lonExpected == null) null else Coordinate(latExpected, lonExpected)

        val actual = Geology.triangulateDestination(pointA, bearingA, pointB, bearingB)

        if (expected == null) {
            assertThat(actual).isNull()
        } else {
            assertThat(actual).isNotNull().isCloseTo(expected, 20f)
        }
    }

    @Test
    fun deadReckon() {
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(280f)
        val distance = 10000f

        val expected = Coordinate(39.984444, 10.115556)
        val actual = Geology.deadReckon(start, distance, bearing)

        assertThat(actual).isCloseTo(expected, 20f)
    }

    @Test
    fun navigate() {
        val start = Coordinate(0.0, 1.0)
        val end = Coordinate(10.0, -8.0)

        val vector = Geology.navigate(start, end, 0f, true)

        assertThat(vector.direction.value).isCloseTo(Bearing(-41.7683f).value, 0.005f)
        assertThat(vector.distance).isCloseTo(1488793.6f, 0.005f)
    }

    @Test
    fun getDeclination() {
        val ny = Coordinate(40.7128, -74.0060)
        val altitude = 10f
        val dec = Geology.getGeomagneticDeclination(ny, altitude, 1608151299005)
        assertEquals(-12.708426f, dec, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideAvalancheRisk")
    fun getAvalancheRisk(angle: Float, expected: AvalancheRisk) {
        val risk = Geology.getAvalancheRisk(angle)
        assertEquals(expected, risk)
    }

    @Test
    fun getMapDistanceVerbal() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val scaleFrom = Distance(2f, DistanceUnits.Centimeters)
        val scaleTo = Distance(0.5f, DistanceUnits.Kilometers)

        val expected = Distance(0.635f, DistanceUnits.Kilometers)

        val actual = Geology.getMapDistance(measurement, scaleFrom, scaleTo)

        assertThat(actual).isCloseTo(expected, 0.001f)
    }

    @Test
    fun getMapDistanceRatio() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val ratioFrom = 0.5f
        val ratioTo = 1.25f

        val expected = Distance(2.5f, DistanceUnits.Inches)

        val actual = Geology.getMapDistance(measurement, ratioFrom, ratioTo)

        assertThat(actual).isCloseTo(expected, 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideCrossTrack")
    fun crossTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate, expected: Float) {
        val actual = Geology.getCrossTrackDistance(point, start, end)

        assertThat(actual).isCloseTo(Distance.meters(expected), 1f)
    }

    @Test
    fun elevationGainLoss() {
        val elevations = listOf(5f, 1f, 2f, 4f, 2f, 4f, 4f).map { Distance.meters(it) }
        val expectedGain = Distance.meters(5f)
        val expectedLoss = Distance.meters(-6f)

        val gain = Geology.getElevationGain(elevations)
        val loss = Geology.getElevationLoss(elevations)

        assertThat(gain).isEqualTo(expectedGain)
        assertThat(loss).isEqualTo(expectedLoss)
    }

    @ParameterizedTest
    @MethodSource("provideHeights")
    fun calculateHeights(distance: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val height = Geology.getHeightFromInclination(Distance.meters(distance), downAngle, upAngle)
        assertEquals(expected, height.distance, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideDistances")
    fun calculateDistance(height: Float, upAngle: Float, downAngle: Float, expected: Float) {
        val d = Geology.getDistanceFromInclination(Distance.meters(height), downAngle, upAngle)
        assertEquals(expected, d.distance, 0.01f)
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
    fun getSlopeGradeFromDistance(vertical: Distance, horizontal: Distance, expected: Float) {
        val grade = Geology.getSlopeGrade(vertical, horizontal)
        assertEquals(expected, grade, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideAlongTrack")
    fun getAlongTrackDistance(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate,
        expected: Float
    ) {
        val actual = Geology.getAlongTrackDistance(point, start, end)
        assertThat(actual).isCloseTo(Distance.meters(expected), 1f)
    }

    @ParameterizedTest
    @MethodSource("provideNearestPoint")
    fun getNearestPoint(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate,
        expected: Coordinate
    ) {
        val actual = Geology.getNearestPoint(point, start, end)
        assertThat(actual).isCloseTo(expected, 1f)
    }

    @ParameterizedTest
    @MethodSource("provideBounds")
    fun getBounds(points: List<Coordinate>, expected: CoordinateBounds) {
        val actual = Geology.getBounds(points)
        assertThat(actual.north).isCloseTo(expected.north, 0.0001)
        assertThat(actual.south).isCloseTo(expected.south, 0.0001)
        assertThat(actual.east).isCloseTo(expected.east, 0.0001)
        assertThat(actual.west).isCloseTo(expected.west, 0.0001)
    }

    companion object {

        @JvmStatic
        fun provideBounds(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    listOf(Coordinate(0.0, 10.0), Coordinate(0.0, 20.0), Coordinate(0.0, 40.0)),
                    CoordinateBounds(0.0, 40.0, 0.0, 10.0)
                ),
                Arguments.of(
                    listOf(Coordinate(-10.0, 0.0), Coordinate(0.0, 0.0), Coordinate(10.0, 0.0)),
                    CoordinateBounds(10.0, 0.0, -10.0, 0.0)
                ),
                Arguments.of(
                    listOf(Coordinate(0.0, 40.0), Coordinate(0.0, 10.0), Coordinate(0.0, 20.0)),
                    CoordinateBounds(0.0, 40.0, 0.0, 10.0)
                ),
                Arguments.of(
                    listOf(Coordinate(0.0, 0.0), Coordinate(-10.0, 0.0), Coordinate(10.0, 0.0)),
                    CoordinateBounds(10.0, 0.0, -10.0, 0.0)
                ),
                Arguments.of(
                    listOf(Coordinate(10.0, 1.0), Coordinate(20.0, -5.0), Coordinate(30.0, 8.0)),
                    CoordinateBounds(30.0, 8.0, 10.0, -5.0)
                ),
                Arguments.of(
                    listOf(Coordinate(10.0, -120.0), Coordinate(20.0, 120.0)),
                    CoordinateBounds(20.0, -120.0, 10.0, 120.0)
                ),
                Arguments.of(
                    listOf(
                        Coordinate(10.0, -120.0),
                        Coordinate(20.0, 120.0),
                        Coordinate(20.0, 0.0)
                    ),
                    CoordinateBounds(20.0, 0.0, 10.0, 120.0)
                ),
                Arguments.of(
                    listOf(
                        Coordinate(10.0, -160.0),
                        Coordinate(20.0, 160.0)
                    ),
                    CoordinateBounds(20.0, -160.0, 10.0, 160.0)
                ),
                Arguments.of(
                    listOf(Coordinate(-90.0, 180.0), Coordinate(90.0, -180.0)),
                    CoordinateBounds(90.0, 180.0, -90.0, -180.0)
                ),
                Arguments.of(
                    listOf(Coordinate(-90.0, 179.999999999), Coordinate(90.0, -179.999999999)),
                    CoordinateBounds(90.0, 179.999999999, -90.0, -179.999999999)
                ),
                Arguments.of(
                    listOf(Coordinate(10.0, 1.0)),
                    CoordinateBounds(10.0, 1.0, 10.0, 1.0)
                ),
                Arguments.of(
                    emptyList<Coordinate>(),
                    CoordinateBounds.empty
                ),
            )
        }

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
                Arguments.of(Distance.meters(1f), Distance.meters(1f), 100f),
                Arguments.of(Distance.feet(20f), Distance.feet(1f), 5f),
                Arguments.of(Distance.feet(20f), Distance.feet(1f).meters(), 5f),
                Arguments.of(Distance.feet(0f), Distance.feet(10f), Float.POSITIVE_INFINITY),
                Arguments.of(Distance.feet(0f), Distance.feet(0f), 0f),
                Arguments.of(Distance.feet(0f), Distance.feet(-10f), Float.NEGATIVE_INFINITY),
                Arguments.of(Distance.feet(20f), Distance.feet(-1f).meters(), -5f),
                Arguments.of(Distance.feet(20f), Distance.feet(-1f), -5f),
                Arguments.of(Distance.meters(1f), Distance.meters(-1f), -100f),
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
        fun provideAltitudes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, 1000f, 0f),
                Arguments.of(1100f, 1000f, -811.3525f),
                Arguments.of(900f, 1000f, 879.9459f),
            )
        }

        @JvmStatic
        fun provideAlongTrack(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Coordinate(53.2611, -0.7972),
                    Coordinate(53.3206, -1.7297),
                    Coordinate(53.1887, 0.1334),
                    Distance.kilometers(62.333f).meters().distance
                ),
                Arguments.of(
                    Coordinate(1.0, 1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(1.11198e5f).distance
                ),
                Arguments.of(
                    Coordinate(-1.0, 1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(1.11198e5f).distance
                ),
                Arguments.of(
                    Coordinate(-1.0, -1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(-1.11198e5f).distance
                ),
                Arguments.of(
                    Coordinate(1.0, -1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(-1.11198e5f).distance
                )
            )
        }

        @JvmStatic
        fun provideNearestPoint(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Coordinate(0.5, -0.5),
                    Coordinate.zero,
                    Coordinate(1.0, -1.0),
                    Coordinate(0.49835, -0.50165)
                ),
                Arguments.of(
                    Coordinate(0.8, -0.5),
                    Coordinate.zero,
                    Coordinate(1.0, -1.0),
                    Coordinate(0.64785, -0.65216)
                ),
                Arguments.of(
                    Coordinate(1.2, -0.5),
                    Coordinate.zero,
                    Coordinate(1.0, -1.0),
                    Coordinate(0.84719, -0.85286)
                ),
                Arguments.of(
                    Coordinate(0.0, 0.5),
                    Coordinate.zero,
                    Coordinate(1.0, -1.0),
                    Coordinate.zero
                ),
                Arguments.of(
                    Coordinate(1.0, -1.5),
                    Coordinate.zero,
                    Coordinate(1.0, -1.0),
                    Coordinate(1.0, -1.0)
                ),
                Arguments.of(
                    Coordinate(1.0, 1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Coordinate(0.0, 1.0)
                ),
                Arguments.of(
                    Coordinate(-1.0, 1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Coordinate(0.0, 1.0)
                ),
                Arguments.of(
                    Coordinate(-1.0, -1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Coordinate.zero
                ),
                Arguments.of(
                    Coordinate(1.0, -1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Coordinate.zero
                ),
                Arguments.of(
                    Coordinate(1.0, 3.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Coordinate(0.0, 2.0)
                )
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