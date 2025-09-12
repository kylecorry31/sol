package com.kylecorry.sol.units

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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

}