package com.kylecorry.sol.science.geophysics

import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VerticalDeflectionCalculatorTest {

    @Test
    fun roundTrip() {
        val geodetic = Coordinate(41.824, -71.4128)
        val deflection = VerticalDeflection(-5.31, -2.94)
        val astronomic = VerticalDeflectionCalculator.toAstronomic(geodetic, deflection)
        assertEquals(41.824 - 5.31 / 3600, astronomic.latitude, 1e-9)
        val back = VerticalDeflectionCalculator.toGeodetic(astronomic, deflection)
        assertEquals(geodetic.latitude, back.latitude, 1e-9)
        assertEquals(geodetic.longitude, back.longitude, 1e-8)
    }

    @Test
    fun fromGeoidSlope() {
        // A geoid rising 1 m per km to the north tilts the plumb line south: xi = -1 mm/m
        val model = VerticalDeflectionCalculator.fromGeoid(0.1) { location ->
            location.latitude * Math.toRadians(1.0) * Geology.EARTH_AVERAGE_RADIUS / 1000.0
        }
        val deflection = model.getVerticalDeflection(Coordinate(40.0, -70.0))!!
        assertEquals(Math.toDegrees(-0.001) * 3600, deflection.xi, 0.01)
        assertEquals(0.0, deflection.eta, 1e-9)
    }
}
