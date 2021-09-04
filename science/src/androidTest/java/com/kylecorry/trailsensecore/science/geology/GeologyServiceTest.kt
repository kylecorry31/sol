package com.kylecorry.trailsensecore.science.geology

import android.location.Location
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate

import org.junit.Assert.*
import org.junit.Test

class GeologyServiceTest {

    private val service = GeologyService()

    @Test
    fun getDeclination() {
        val ny = Coordinate(40.7128, -74.0060)
        val altitude = 10f
        val dec = service.getMagneticDeclination(ny, altitude, 1608151299005)
        assertEquals(-12.820191f, dec, 0.01f)
    }

    @Test
    fun triangulate(){
        val pointA = Coordinate(40.0, 10.0)
        val bearingA = Bearing(220f)
        val pointB = Coordinate(40.5, 9.5)
        val bearingB = Bearing(295f)

        val expected = Coordinate(40.229722, 10.252778)
        val actual = service.triangulate(pointA, bearingA, pointB, bearingB)

        assertNotNull(actual)
        assertEquals(expected.latitude, actual!!.latitude, 0.01)
        assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @Test
    fun deadReckon(){
        val start = Coordinate(40.0, 10.0)
        val bearing = Bearing(280f)
        val distance = 10000f

        val expected = Coordinate(39.984444, 10.115556)
        val actual = service.deadReckon(start, distance, bearing)
        assertEquals(expected.latitude, actual.latitude, 0.01)
        assertEquals(expected.longitude, actual.longitude, 0.01)
    }

    @Test
    fun navigate() {
        val start = Coordinate(0.0, 1.0)
        val end = Coordinate(10.0, -8.0)

        val vector = service.navigate(start, end, 0f, true)

        val expected = FloatArray(3)
        Location.distanceBetween(0.0, 1.0, 10.0, -8.0, expected)

        assertEquals(Bearing(expected[1]).value, vector.direction.value, 0.005f)
        assertEquals(expected[0], vector.distance, 0.005f)
    }
}