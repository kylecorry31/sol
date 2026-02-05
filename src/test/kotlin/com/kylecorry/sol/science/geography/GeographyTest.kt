package com.kylecorry.sol.science.geography

import assertk.assertThat
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.CoordinateBounds
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.tests.isCloseTo
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GeographyTest {

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 6378137, 0, 0",
        "10, 20, 10, 5903039.0, 2148530.5, 1100250.2"
    )
    fun getECEF(
        latitude: Double,
        longitude: Double,
        elevation: Float,
        expectedX: Float,
        expectedY: Float,
        expectedZ: Float
    ) {
        val coordinate = Coordinate(latitude, longitude)
        val ecef = Geography.getECEF(Location(coordinate, Distance.meters(elevation)))
        assertEquals(expectedX, ecef.x, 0.001f)
        assertEquals(expectedY, ecef.y, 0.001f)
        assertEquals(expectedZ, ecef.z, 0.001f)
    }

    @ParameterizedTest
    @CsvSource(
        "6378137, 0, 0, 0, 0, 0",
        "5903039.0, 2148530.5, 1100250.2, 10, 20, 10"
    )
    fun getLocationFromECEF(
        x: Float,
        y: Float,
        z: Float,
        expectedLatitude: Double,
        expectedLongitude: Double,
        expectedElevation: Float
    ) {
        val coordinate = Geography.getLocationFromECEF(Vector3(x, y, z))
        assertEquals(expectedLatitude, coordinate.coordinate.latitude, 0.1)
        assertEquals(expectedLongitude, coordinate.coordinate.longitude, 0.1)
        assertEquals(expectedElevation, coordinate.elevation.meters().value, 0.1f)
    }

    @Test
    fun trilaterate() {
        val locations = listOf(
            Geofence(
                Coordinate(37.418436, -121.963477),
                Distance.kilometers(0.265710701754f)
            ),
            Geofence(
                Coordinate(37.417243, -121.961889),
                Distance.kilometers(0.234592423446f)
            ),
            Geofence(
                Coordinate(37.418692, -121.960194),
                Distance.kilometers(0.0548954278262f)
            )
        )

        val prediction = Geography.trilaterate(locations)
        val expected = Coordinate(37.417959, -121.961954)
        assertEquals(expected.latitude, prediction.locations.first().latitude, 0.01)
        assertEquals(expected.longitude, prediction.locations.first().longitude, 0.01)

        val locations2 = listOf(
            Geofence(
                Coordinate(37.673442, -90.234036),
                Distance.nauticalMiles(107.5f)
            ),
            Geofence(
                Coordinate(36.109997, -90.953669),
                Distance.nauticalMiles(145f)
            ),
        )

        val prediction2 = Geography.trilaterate(locations2)
        val expected2 = listOf(Coordinate(36.989311, -88.151426), Coordinate(38.238380, -92.390485))
        assertEquals(expected2.size, prediction2.locations.size)
        assertEquals(expected2[0].latitude, prediction2.locations[0].latitude, 0.001)
        assertEquals(expected2[0].longitude, prediction2.locations[0].longitude, 0.001)
        assertEquals(expected2[1].latitude, prediction2.locations[1].latitude, 0.001)
        assertEquals(expected2[1].longitude, prediction2.locations[1].longitude, 0.001)
    }

    @Test
    fun triangulateSelf() {
        val pointA = Coordinate(40.0, 10.0)
        val bearingA = Bearing.from(220f)
        val pointB = Coordinate(40.5, 9.5)
        val bearingB = Bearing.from(295f)

        val expected = Coordinate(40.229722, 10.252778)
        val actual = Geography.triangulateSelf(pointA, bearingA, pointB, bearingB)

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
        val bearingA = Bearing.from(degA)
        val pointB = Coordinate(latB, lonB)
        val bearingB = Bearing.from(degB)
        val expected = if (latExpected == null || lonExpected == null) null else Coordinate(latExpected, lonExpected)

        val actual = Geography.triangulateDestination(pointA, bearingA, pointB, bearingB)

        if (expected == null) {
            assertThat(actual).isNull()
        } else {
            assertThat(actual).isNotNull().isCloseTo(expected, 20f)
        }
    }

    @Test
    fun deadReckon() {
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing.from(280f)
        val distance = 10000f

        val expected = Coordinate(39.984444, 10.115556)
        val actual = Geography.deadReckon(start, distance, bearing)

        assertThat(actual).isCloseTo(expected, 20f)
    }

    @Test
    fun navigate() {
        val start = Coordinate(0.0, 1.0)
        val end = Coordinate(10.0, -8.0)

        val vector = Geography.navigate(start, end, 0f, true)

        assertThat(vector.direction.value).isCloseTo(Bearing.from(-41.7683f).value, 0.005f)
        assertThat(vector.distance).isCloseTo(1488793.6f, 0.005f)
    }

    @Test
    fun getMapDistanceVerbal() {
        val measurement = Distance.from(1f, DistanceUnits.Inches)
        val scaleFrom = Distance.from(2f, DistanceUnits.Centimeters)
        val scaleTo = Distance.from(0.5f, DistanceUnits.Kilometers)

        val expected = Distance.from(0.635f, DistanceUnits.Kilometers)

        val actual = Geography.getMapDistance(measurement, scaleFrom, scaleTo)

        assertThat(actual).isCloseTo(expected, 0.001f)
    }

    @Test
    fun getMapDistanceRatio() {
        val measurement = Distance.from(1f, DistanceUnits.Inches)
        val ratioFrom = 0.5f
        val ratioTo = 1.25f

        val expected = Distance.from(2.5f, DistanceUnits.Inches)

        val actual = Geography.getMapDistance(measurement, ratioFrom, ratioTo)

        assertThat(actual).isCloseTo(expected, 0.001f)
    }

    @ParameterizedTest
    @MethodSource("provideCrossTrack")
    fun crossTrackDistance(point: Coordinate, start: Coordinate, end: Coordinate, expected: Float) {
        val actual = Geography.getCrossTrackDistance(point, start, end)

        assertThat(actual).isCloseTo(Distance.meters(expected), 1f)
    }

    @Test
    fun elevationGainLoss() {
        val elevations = listOf(5f, 1f, 2f, 4f, 2f, 4f, 4f).map { Distance.meters(it) }
        val expectedGain = Distance.meters(5f)
        val expectedLoss = Distance.meters(-6f)

        val gain = Geography.getElevationGain(elevations)
        val loss = Geography.getElevationLoss(elevations)

        assertThat(gain).isEqualTo(expectedGain)
        assertThat(loss).isEqualTo(expectedLoss)
    }

    @ParameterizedTest
    @MethodSource("provideAlongTrack")
    fun getAlongTrackDistance(
        point: Coordinate,
        start: Coordinate,
        end: Coordinate,
        expected: Float
    ) {
        val actual = Geography.getAlongTrackDistance(point, start, end)
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
        val actual = Geography.getNearestPoint(point, start, end)
        assertThat(actual).isCloseTo(expected, 1f)
    }

    @ParameterizedTest
    @MethodSource("provideBounds")
    fun getBounds(points: List<Coordinate>, expected: CoordinateBounds) {
        val actual = Geography.getBounds(points)
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
        fun provideAlongTrack(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Coordinate(53.2611, -0.7972),
                    Coordinate(53.3206, -1.7297),
                    Coordinate(53.1887, 0.1334),
                    Distance.kilometers(62.333f).meters().value
                ),
                Arguments.of(
                    Coordinate(1.0, 1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(1.11198e5f).value
                ),
                Arguments.of(
                    Coordinate(-1.0, 1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(1.11198e5f).value
                ),
                Arguments.of(
                    Coordinate(-1.0, -1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(-1.11198e5f).value
                ),
                Arguments.of(
                    Coordinate(1.0, -1.0),
                    Coordinate.zero,
                    Coordinate(0.0, 2.0),
                    Distance.meters(-1.11198e5f).value
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
    }
}