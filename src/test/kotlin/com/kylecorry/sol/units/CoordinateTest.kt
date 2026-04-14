package com.kylecorry.sol.units

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CoordinateTest {

    @Test
    fun canRestrictLongitude(){
        assertThat(Coordinate.toLongitude(10.0)).isEqualTo(10.0)
        assertThat(Coordinate.toLongitude(-180.0)).isEqualTo(-180.0)
        assertThat(Coordinate.toLongitude(180.0)).isEqualTo(180.0)
        assertThat(Coordinate.toLongitude(200.0)).isEqualTo(-160.0)
        assertThat(Coordinate.toLongitude(-200.0)).isEqualTo(160.0)
        assertThat(Coordinate.toLongitude(570.0)).isEqualTo(-150.0)
        assertThat(Coordinate.toLongitude(-660.0)).isEqualTo(60.0)
    }

    @Test
    fun isNorthernHemisphere() {
        assertTrue(Coordinate(1.0, 0.0).isNorthernHemisphere)
        assertFalse(Coordinate(-1.0, 0.0).isNorthernHemisphere)
        assertTrue(Coordinate(0.0, 0.0).isNorthernHemisphere)
    }

    @ParameterizedTest
    @CsvSource(
        "100.0, 200.0, 90.0, -160.0",
        "-100.0, -200.0, -90.0, 160.0",
        "42.815, -70.8733, 42.815, -70.8733",
        "0.0, 0.0, 0.0, 0.0",
        "90.0, 180.0, 90.0, 180.0",
        "-90.0, -180.0, -90.0, -180.0",
        "89.9999999, 179.9999999, 89.9999999, 179.9999999",
        "-89.9999999, -179.9999999, -89.9999999, -179.9999999",
        "45.123456789, -179.999999999, 45.123456789, -179.999999999",
        "-45.987654321, 179.999999999, -45.987654321, 179.999999999",
        "12.345678901, 540.0, 12.345678901, -180.0",
        "12.345678901, -540.0, 12.345678901, 180.0",
        "12.345678901, 539.999999999, 12.345678901, 179.999999999",
        "12.345678901, -539.999999999, 12.345678901, -179.999999999"
    )
    fun constructorConstrainsAndRoundTrips(
        latitude: Double,
        longitude: Double,
        expectedLatitude: Double,
        expectedLongitude: Double
    ) {
        val coordinate = Coordinate(latitude, longitude)

        assertEquals(expectedLatitude, coordinate.latitude, 0.000001)
        assertEquals(expectedLongitude, coordinate.longitude, 0.000001)
    }

    @Test
    fun canConvertDoubleCoordinate() {
        val coordinate = DoubleCoordinate(100.0, 200.0).toCoordinate()

        assertEquals(90.0, coordinate.latitude, 0.000001)
        assertEquals(-160.0, coordinate.longitude, 0.000001)
    }

    @Test
    fun canAddDistance() {
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing.from(100f)
        val distance = 10000.0

        val expected = Coordinate(39.984444, 10.115556)
        val actual = start.plus(distance, bearing)
        assertEquals(expected.latitude, actual.latitude, 0.01)
        assertEquals(expected.longitude, actual.longitude, 0.01)
    }

}
