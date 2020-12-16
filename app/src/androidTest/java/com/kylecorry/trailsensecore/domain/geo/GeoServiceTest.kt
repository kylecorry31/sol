package com.kylecorry.trailsensecore.domain.geo

import org.junit.Test

import org.junit.Assert.*

class GeoServiceTest {

    @Test
    fun getDeclination() {
        val service = GeoService()
        val ny = Coordinate(40.7128, -74.0060)
        val altitude = 10f
        val dec = service.getDeclination(ny, altitude, 1608151299005)
        assertEquals(-12.820191f, dec, 0.01f)
    }
}