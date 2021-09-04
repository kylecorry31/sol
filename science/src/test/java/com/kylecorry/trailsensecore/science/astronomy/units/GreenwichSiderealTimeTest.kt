package com.kylecorry.trailsensecore.science.astronomy.units

import com.kylecorry.trailsensecore.tests.assertDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class GreenwichSiderealTimeTest {

    @Test
    fun toSiderealTime() {
        val ut = UniversalTime.of(2010, Month.FEBRUARY, 7, 23, 30)
        val gst = ut.toSiderealTime()
        assertEquals(gst.hours, 8.698091, 0.0000005)
    }

    @Test
    fun siderealToUniversalTime() {
        val gst = GreenwichSiderealTime(8.698056)
        val ut = gst.toUniversalTime(LocalDate.of(2010, Month.FEBRUARY, 7))
        assertDate(LocalDateTime.of(2010, Month.FEBRUARY, 7, 23, 30), ut, Duration.ofMillis(100))
    }

    @Test
    fun atLongitude(){
        val gst = GreenwichSiderealTime(2.061389)
        val lst = gst.atLongitude(-40.0)
        assertEquals(23.394722, lst.hours, 0.000001)
        assertEquals(-40.0, lst.longitude, 0.000001)
    }
}