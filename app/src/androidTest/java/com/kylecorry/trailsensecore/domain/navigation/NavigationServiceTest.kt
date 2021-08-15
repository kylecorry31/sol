package com.kylecorry.trailsensecore.domain.navigation

import android.graphics.Color
import android.location.Location
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationServiceTest {

    private val service = NavigationService()

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

    @Test
    fun nearby() {
        val mtWashington = Coordinate(44.2706, -71.3036)
        val beacons = listOf(
            Beacon(0, "Tip top house", Coordinate(44.2705, -71.3036), color = Color.BLACK),
            Beacon(1, "Crawford", Coordinate(44.2709, -71.3056), color = Color.BLACK),
            Beacon(2, "Pinkham", Coordinate(44.2571, -71.2530), color = Color.BLACK)
        )

        val near5km = service.nearby(mtWashington, beacons, 5000f).map { it.id }
        val near500m = service.nearby(mtWashington, beacons, 500f).map { it.id }

        assertEquals(listOf(0L, 1L, 2L), near5km)
        assertEquals(listOf(0L, 1L), near500m)
    }

    @Test
    fun eta(){
        val location = Coordinate(44.2571, -71.2530)
        val speed = 1.5f
        val altitude = 1000f

        val destination = Coordinate(44.2706, -71.3036)
        val destinationAltitude = 1900f
        val beacon = Beacon(0, "", destination, elevation = destinationAltitude, color = Color.BLACK)

        val linearEta = service.eta(Position(location, altitude, Bearing(0f), speed), beacon, false)
        val nonLinearEta = service.eta(Position(location, altitude, Bearing(0f), speed), beacon, true)

        val linearEtaDownhill = service.eta(Position(location, destinationAltitude, Bearing(0f), speed), beacon.copy(elevation = altitude), false)
        val nonLinearEtaDownhill = service.eta(Position(location, destinationAltitude, Bearing(0f), speed), beacon.copy(elevation = altitude), true)

        assertEquals(137L, linearEta.toMinutes())
        assertEquals(165L, nonLinearEta.toMinutes())
        assertEquals(47L, linearEtaDownhill.toMinutes())
        assertEquals(75L, nonLinearEtaDownhill.toMinutes())
    }
}