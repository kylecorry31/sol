package com.kylecorry.trailsensecore.domain.navigation.geo

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.geo.DeclinationCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

internal class DeclinationCalculatorTest {

    @Test
    fun calculate() {
        val ny = Coordinate(40.7128, -74.0060)
        val altitude = 10f

        val calculator = DeclinationCalculator()
        val dec = calculator.calculate(ny, altitude)

        assertEquals(-13f, dec, 1f)
    }
}