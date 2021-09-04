package com.kylecorry.trailsensecore.astronomy.units

import com.kylecorry.trailsensecore.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.astronomy.units.GreenwichSiderealTime
import com.kylecorry.trailsensecore.astronomy.units.HorizonCoordinate
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class HorizonCoordinateTest {

    @Test
    fun toEquatorial() {
        val horizon = HorizonCoordinate(115.0, 40.0)
        val latitude = 38.0
        val siderealTime = GreenwichSiderealTime(0.0)

        val equatorial = horizon.toEquatorial(siderealTime, latitude)

        assertEquals(21.031560, equatorial.getHourAngle(siderealTime), 0.00001)
        assertEquals(8.084044, equatorial.declination, 0.00001)
    }

    @Test
    fun fromEquatorial() {
        val siderealTime = GreenwichSiderealTime(0.0)
        val equatorial = EquatorialCoordinate.fromHourAngle(-0.508333, 16.495833, siderealTime)
        val latitude = 25.0

        val horizon = HorizonCoordinate.fromEquatorial(equatorial, siderealTime, latitude)

        assertEquals(80.525393, horizon.azimuth, 0.00001)
        assertEquals(-20.577738, horizon.altitude, 0.00001)
    }
}