package com.kylecorry.trailsensecore.domain.astronomy.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EclipticCoordinateTest {

    @Test
    fun toEquatorial() {
        val ut = UniversalTime.of(2000, 1, 1, 0, 0).minusDays(1)
        val ecliptic = EclipticCoordinate(1.2, 184.6)
        val equatorial = ecliptic.toEquatorial(ut)

        assertEquals(184.697898, equatorial.rightAscension, 0.00001)
        assertEquals(-0.726531, equatorial.declination, 0.00001)
    }

    @Test
    fun fromEquatorial() {
        val ut = UniversalTime.of(2000, 1, 1, 0, 0).minusDays(1)
        val equatorial = EquatorialCoordinate(-0.726528, 184.697917)
        val ecliptic = EclipticCoordinate.fromEquatorial(equatorial, ut)

        assertEquals(1.20001, ecliptic.eclipticLatitude, 0.00001)
        assertEquals(184.600016, ecliptic.eclipticLongitude, 0.00001)
    }

    @Test
    fun getObliquityOfTheEcliptic() {
        val ut = UniversalTime.of(2010, 1, 1, 0, 0).minusDays(1)
        val e2010 = EclipticCoordinate.getObliquityOfTheEcliptic(ut)
        assertEquals(23.437992, e2010, 0.000001)
    }

}