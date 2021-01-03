package com.kylecorry.trailsensecore.domain.geo

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.DistanceUnits
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class GeoServiceTest2 {

    @Test
    fun getMapDistanceVerbal() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val scaleFrom = Distance(2f, DistanceUnits.Centimeters)
        val scaleTo = Distance(0.5f, DistanceUnits.Kilometers)

        val expected = Distance(0.635f, DistanceUnits.Kilometers)

        val service = GeoService()
        val actual = service.getMapDistance(measurement, scaleFrom, scaleTo)

        assertEquals(expected.distance, actual.distance, 0.001f)
        assertEquals(expected.units, actual.units)
    }

    @Test
    fun getMapDistanceRatio() {
        val measurement = Distance(1f, DistanceUnits.Inches)
        val ratioFrom = 0.5f
        val ratioTo = 1.25f

        val expected = Distance(2.5f, DistanceUnits.Inches)

        val service = GeoService()
        val actual = service.getMapDistance(measurement, ratioFrom, ratioTo)

        assertEquals(expected.distance, actual.distance, 0.001f)
        assertEquals(expected.units, actual.units)
    }
}