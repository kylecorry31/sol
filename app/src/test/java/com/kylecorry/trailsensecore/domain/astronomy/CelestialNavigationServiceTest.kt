package com.kylecorry.trailsensecore.domain.astronomy

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.*

internal class CelestialNavigationServiceTest {

    @Test
    fun getLatitudeFromPolaris(){
        val service = CelestialNavigationService()

        val latitude = service.getLatitudeFromPolaris(78.0)

        assertEquals(78.0, latitude, 0.0)
    }

    @Test
    fun getLongitudeFromNoon() {
        val service = CelestialNavigationService()
        val noon = Instant.ofEpochMilli(1606841117093L)

        val longitude = service.getLongitudeFromNoon(noon)

        assertEquals(-74.0060, longitude, 0.05)
    }

    @Test
    fun getLongitudeFromSundial() {
        val service = CelestialNavigationService()
        val noon = LocalTime.NOON
        val utc = Instant.ofEpochMilli(1606841117093L)

        val longitude = service.getLongitudeFromSundial(noon, utc)

        assertEquals(-74.0060, longitude, 0.1)
    }
}