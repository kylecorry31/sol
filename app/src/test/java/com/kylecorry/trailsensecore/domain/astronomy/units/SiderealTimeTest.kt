package com.kylecorry.trailsensecore.domain.astronomy.units
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SiderealTimeTest {

    @Test
    fun toGreenwich() {
        val lst = SiderealTime(23.394722, 50.0)
        val gst = lst.atGreenwich()
        assertEquals(20.061389, gst.hours, 0.000001)
    }

}