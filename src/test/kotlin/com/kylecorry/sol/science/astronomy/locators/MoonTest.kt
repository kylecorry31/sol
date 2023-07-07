package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.units.UniversalTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class MoonTest {

    val moon = Moon()

    @Test
    fun getCoordinates() {
    }

    @Test
    fun getDistance() {
    }

    @Test
    fun getAngularDiameter() {
        val diameter = moon.getAngularDiameter(UniversalTime.of(2015, 2, 15, 0, 0))
        Assertions.assertEquals(0.529422, diameter, 0.0001)
    }

    @Test
    fun getMeanAnomaly() {
    }
}