package com.kylecorry.sol.science.geography

import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.Geofence
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

}