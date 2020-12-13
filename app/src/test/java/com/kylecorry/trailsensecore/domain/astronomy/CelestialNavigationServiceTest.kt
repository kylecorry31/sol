package com.kylecorry.trailsensecore.domain.astronomy

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream

internal class CelestialNavigationServiceTest {

    @ParameterizedTest
    @MethodSource("providePolarisLatitudes")
    fun getLatitudeFromPolaris(polarisAltitude: Float, expectedLatitude: Double){
        val service = CelestialNavigationService()

        val latitude = service.getLatitudeFromPolaris(polarisAltitude)

        assertEquals(expectedLatitude, latitude, 0.0)
    }

    @ParameterizedTest
    @MethodSource("provideNoonLatitudes")
    fun getLatitudeFromNoonNorthern(altitude: Float, noonEpoch: Long, inNorthernHemisphere: Boolean, expectedLatitude: Double) {
        val service = CelestialNavigationService()
        val noon = Instant.ofEpochMilli(noonEpoch)
        val latitude = service.getLatitudeFromNoon(altitude, noon, inNorthernHemisphere)
        assertEquals(expectedLatitude, latitude, 0.05)
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


    companion object {

        @JvmStatic
        fun provideNoonLatitudes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(77.93351f, 1607911088847L, false, -35.293056),
                Arguments.of(27.346266f, 1606841117093L, true, 40.7128),
            )
        }

        @JvmStatic
        fun providePolarisLatitudes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0.0f, 0.0),
                Arguments.of(1.0f, 1.0),
                Arguments.of(90.0f, 90.0),
            )
        }
    }

}