package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.tests.assertDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class GreenwichSiderealTimeTest {

    @Test
    fun toSiderealTime() {
//        val ut = UniversalTime.of(2010, Month.FEBRUARY, 7, 23, 30)
//        val gst = ut.toSiderealTime()
//        assertEquals(gst.hours, 8.698091, 0.0000005)
        val ut = UniversalTime.of(1987, Month.APRIL, 10, 0, 0, 0)
        val gmst = ut.toSiderealTime()
        val gast = ut.toSiderealTime(true)
        assertEquals(timeToDecimal(13, 10, 46.3668), gmst.hours, 0.000001)
        assertEquals(timeToDecimal(13, 10, 46.1351), gast.hours, 0.000001)
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