package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.astronomy.locators.Moon
import com.kylecorry.trailsensecore.domain.astronomy.locators.Sun
import com.kylecorry.trailsensecore.domain.astronomy.units.fromJulianDay
import com.kylecorry.trailsensecore.domain.math.MathUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

class AstroTest {

    @Test
    fun power() {
        val cases = listOf(
            listOf(1.0, 1.0, 1.0),
            listOf(2.0, 1.0, 2.0),
            listOf(2.0, 2.0, 4.0),
            listOf(3.0, 2.0, 9.0),
            listOf(4.0, 3.0, 64.0),
            listOf(2.0, 0.0, 1.0),
            listOf(2.0, -1.0, 0.5),
            listOf(2.0, -2.0, 0.25)
        )

        cases.forEach {
            assertEquals(it[2], com.kylecorry.andromeda.core.math.power(it[0], it[1].toInt()), 0.0000001)
        }
    }

    @Test
    fun polynomial() {
        assertEquals(0.0, MathUtils.polynomial(1.0, 0.0), 0.0001)
        assertEquals(0.0, MathUtils.polynomial(1.0), 0.0001)
        assertEquals(1.0, MathUtils.polynomial(2.0, 1.0), 0.0001)
        assertEquals(3.0, MathUtils.polynomial(1.0, 1.0, 2.0), 0.0001)
        assertEquals(81.0, MathUtils.polynomial(2.0, 1.0, 2.0, 3.0, 0.0, 4.0), 0.0001)
    }

    @Test
    fun reduceAngleDegrees() {
        assertEquals(0.0, Astro.reduceAngleDegrees(0.0), 0.0)
        assertEquals(180.0, Astro.reduceAngleDegrees(180.0), 0.0)
        assertEquals(0.0, Astro.reduceAngleDegrees(0.0), 0.0)
        assertEquals(1.0, Astro.reduceAngleDegrees(361.0), 0.0)
        assertEquals(359.0, Astro.reduceAngleDegrees(-1.0), 0.0)
        assertEquals(180.0, Astro.reduceAngleDegrees(-180.0), 0.0)
        assertEquals(360.0, Astro.reduceAngleDegrees(720.0), 0.0)
    }

    @Test
    fun hourToAngle() {
        assertEquals(
            Math.toDegrees(2.4213389045),
            Astro.timeToAngle(9, 14, 55.8),
            0.00000001
        )
    }

    @Test
    fun interpolate() {
        assertEquals(
            0.876125,
            Astro.interpolate(0.18125, 0.884226, 0.877366, 0.870531),
            0.0000005
        )
    }

    @Test
    fun julianDay() {
        val tolerance = 0.0001

        assertEquals(
            2451545.0,
            Astro.julianDay(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0)),
            tolerance
        )
        assertEquals(
            2451179.5,
            Astro.julianDay(LocalDateTime.of(1999, Month.JANUARY, 1, 0, 0)),
            tolerance
        )
        assertEquals(
            2436116.3097222,
            Astro.julianDay(LocalDateTime.of(1957, Month.OCTOBER, 4, 19, 26)),
            tolerance
        )
        assertEquals(
            2455197.5,
            Astro.julianDay(LocalDateTime.of(2010, 1, 1, 0, 0)),
            tolerance
        )
    }

    @Test
    fun deltaT() {
        assertEquals(65.0, Astro.deltaT(2000), 10.0)
        assertEquals(69.0, Astro.deltaT(2005), 10.0)
        assertEquals(80.0, Astro.deltaT(2015), 10.0)
    }

    @Test
    fun ut() {
        assertEquals(
            LocalDateTime.of(2020, 1, 1, 12, 0),
            Astro.ut(
                ZonedDateTime.of(
                    LocalDateTime.of(2020, 1, 1, 7, 0),
                    ZoneId.of("America/New_York")
                )
            )
        )
    }

    @Test
    fun utToLocal() {
        assertEquals(
            ZonedDateTime.of(
                LocalDateTime.of(2020, 1, 1, 7, 0),
                ZoneId.of("America/New_York")
            ),
            Astro.utToLocal(
                LocalDateTime.of(2020, 1, 1, 12, 0),
                ZoneId.of("America/New_York")
            )
        )
    }


//    @Test
//    fun riseSetTransitTimes() {
//        val times = Astro.riseSetTransitTimes(
//            42.3333, -71.0833, 177.74208, -0.5667, false, 56.0,
//            Triple(18.04761, 18.44092, 18.82742), Triple(40.68021, 41.73129, 42.78204)
//        )
//        assertEquals(0.51766 * 24, times!!.first, 0.05)
//        assertEquals(0.81980 * 24, times.second, 0.05)
//        assertEquals(0.12130 * 24, times.third, 0.05)
//    }

    @Test
    fun solarCoordinates() {
        val coords = Sun().getCoordinates(fromJulianDay(2448908.5))
        assertEquals(-7.78507, coords.declination, 0.0001)
        assertEquals(360 - 161.61917, coords.rightAscension, 0.0001)
    }

    @Test
    fun lunarCoordinates() {
        val coords = Moon().getCoordinates(fromJulianDay(2448724.5))
        assertEquals(13.768368, coords.declination, 0.001)
        assertEquals(134.688470, coords.rightAscension, 0.001)
    }


}