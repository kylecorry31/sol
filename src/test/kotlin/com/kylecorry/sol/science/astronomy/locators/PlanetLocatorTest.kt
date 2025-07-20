package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.units.UniversalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlanetLocatorTest {

    @Test
    fun saturn(){
        val ut = UniversalTime.of(2016, 1, 4, 3, 0)
        val locator = PlanetLocator(Planet.Saturn)
        val coordinates = locator.getCoordinates(ut)
        assertEquals(coordinates.rightAscensionHours, 16.67523, 0.005)
        assertEquals(coordinates.declination, -20.537509, 0.005)
    }

    @Test
    fun venus(){
        val ut = UniversalTime.of(2016, 1, 4, 3, 0)
        val locator = PlanetLocator(Planet.Venus)
        val coordinates = locator.getCoordinates(ut)
        assertEquals(coordinates.rightAscensionHours, 16.283091, 0.005)
        assertEquals(coordinates.declination, -19.407214, 0.005)
    }

}