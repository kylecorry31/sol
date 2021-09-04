package com.kylecorry.trailsensecore.units

import org.junit.Assert
import org.junit.jupiter.api.Test

class CoordinateTest {

    @Test
    fun isNorthernHemisphere() {
        Assert.assertTrue(Coordinate(1.0, 0.0).isNorthernHemisphere)
        Assert.assertFalse(Coordinate(-1.0, 0.0).isNorthernHemisphere)
        Assert.assertFalse(Coordinate(0.0, 0.0).isNorthernHemisphere)
    }

    @Test
    fun canAddDistance() {
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(100f)
        val distance = 10000.0

        val expected = Coordinate(39.984444, 10.115556)
        val actual = start.plus(distance, bearing)
        Assert.assertEquals(expected.latitude, actual.latitude, 0.01)
        Assert.assertEquals(expected.longitude, actual.longitude, 0.01)
    }

}