package com.kylecorry.trailsensecore.domain.units

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DistanceTest {

    @Test
    fun convertTo() {
        val distance = Distance(1f, DistanceUnits.Centimeters)
        val expected = Distance(0.00001f, DistanceUnits.Kilometers)
        assertEquals(expected, distance.convertTo(DistanceUnits.Kilometers))
    }
}