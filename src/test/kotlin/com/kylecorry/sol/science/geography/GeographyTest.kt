package com.kylecorry.sol.science.geography

import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Location
import org.junit.jupiter.api.Assertions.*
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
        assertEquals(expectedElevation, coordinate.elevation.meters().distance, 0.1f)
    }

}