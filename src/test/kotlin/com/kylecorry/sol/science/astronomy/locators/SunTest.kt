package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.units.UniversalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SunTest {

    val sun = Sun()

    @Test
    fun getCoordinates() {
        val coords = sun.getCoordinates(UniversalTime.of(1992, 10, 13, 0, 0))
        assertEquals(-7.785303541399693, coords.declination, 0.0001)
        assertEquals(198.3814040423241, coords.rightAscension, 0.0001)
    }

    @Test
    fun getDistance() {
        val dist = sun.getDistance(UniversalTime.of(2021, 9, 3, 0, 0))
        assertEquals(150915932.659f, dist.amount, 1000f)

        val dist2 = sun.getDistance(UniversalTime.of(2015, 2, 15, 0, 0))
        assertEquals(1.478E08f, dist2.amount, 100000f)
    }

    @Test
    fun getAngularDiameter() {
        val diameter = sun.getAngularDiameter(UniversalTime.of(2015, 2, 15, 0, 0))
        assertEquals(0.539790, diameter, 0.0001)
    }

    @Test
    fun getMeanAnomaly() {
        val mean = sun.getMeanAnomaly(UniversalTime.of(1992, 10, 13, 0, 0))
        assertEquals(278.99397, mean, 0.0001)
    }
}