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

    @ParameterizedTest
    @CsvSource(
        "0.0, 0.0, 0.0, 0.0",
        "1.0, 1.0, 1.0, 1.0",
        "90.0, 180.0, 90.0, 180.0",
        "-90.0, -180.0, -90.0, -180.0",
        "89.9999999, 179.9999999, 89.9999999, 179.9999999",
        "-89.9999999, -179.9999999, -89.9999999, -179.9999999",
        "89.5555555, 179.5555555, 89.5555555, 179.5555555",
        "-89.5555555, -179.5555555, -89.5555555, -179.5555555",
        "1.23456789, 0.12345671, 1.2345679, 0.1234567",
    )
    fun properlyPacksIntoALong(inputLatitude: Double, inputLongitude: Double, outputLatitude: Double, outputLongitude: Double) {
        val coordinate = Coordinate(inputLatitude, inputLongitude)
        assertEquals(outputLatitude, coordinate.latitude)
        assertEquals(outputLongitude, coordinate.longitude)
    }

}