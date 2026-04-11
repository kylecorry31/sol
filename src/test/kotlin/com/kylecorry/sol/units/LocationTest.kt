package com.kylecorry.sol.units

import com.kylecorry.sol.science.geology.Geology
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LocationTest {

    @Test
    fun verticalDistanceTo() {
        val start = Location(Coordinate(40.0, 10.0), Distance.meters(100f))
        val end = Location(Coordinate(40.0, 10.0), Distance.meters(250f))

        assertEquals(150f, start.verticalDistanceTo(end), 0.00001f)
    }

    @Test
    fun inclinationTo() {
        val startCoordinate = Coordinate(40.0, 10.0)
        val endCoordinate = startCoordinate.plus(1000.0, Bearing.from(90f))
        val start = Location(startCoordinate, Distance.meters(100f))
        val end = Location(endCoordinate, Distance.meters(200f))

        val expected = Geology.getInclination(
            Distance.meters(start.horizontalDistanceTo(end)),
            Distance.meters(100f)
        )

        assertEquals(expected, start.inclinationTo(end), 0.00001f)
    }
}
