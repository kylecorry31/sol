package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.Coordinate
import org.junit.Test

import org.junit.Assert.*
import java.time.*

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
            assertEquals(it[2], Astro.power(it[0], it[1].toInt()), 0.0000001)
        }
    }

    @Test
    fun polynomial() {
        assertEquals(0.0, Astro.polynomial(1.0, 0.0), 0.0001)
        assertEquals(0.0, Astro.polynomial(1.0), 0.0001)
        assertEquals(1.0, Astro.polynomial(2.0, 1.0), 0.0001)
        assertEquals(3.0, Astro.polynomial(1.0, 1.0, 2.0), 0.0001)
        assertEquals(81.0, Astro.polynomial(2.0, 1.0, 2.0, 3.0, 0.0, 4.0), 0.0001)
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
    fun interpolateExtremum() {
        assertEquals(
            1.3812030,
            Astro.interpolateExtremum(1.3814294, 1.3812213, 1.3812453),
            0.0000005
        )
    }

    @Test
    fun interpolateExtremumX() {
        assertEquals(
            0.3966,
            Astro.interpolateExtremumX(1.3814294, 1.3812213, 1.3812453),
            0.00005
        )
    }

    @Test
    fun interpolateZeroCrossing() {
        assertEquals(
            -0.20127,
            Astro.interpolateZeroCrossing(-1693.4, 406.3, 2303.2),
            0.000005
        )
    }

    @Test
    fun canInterpolate() {
        assertTrue(Astro.canInterpolate(-1693.4, 406.3, 2303.2, 203.0))
        assertFalse(Astro.canInterpolate(-1693.4, 406.3, 2303.2, 201.0))
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
    fun tt() {
        assertEquals(
            LocalDateTime.of(2000, 1, 1, 12, 1, 5),
            Astro.tt(
                ZonedDateTime.of(
                    LocalDateTime.of(2000, 1, 1, 7, 0),
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

    @Test
    fun ttToLocal() {
        assertEquals(
            ZonedDateTime.of(
                LocalDateTime.of(2000, 1, 1, 7, 0),
                ZoneId.of("America/New_York")
            ),
            Astro.ttToLocal(
                LocalDateTime.of(2000, 1, 1, 12, 1, 5),
                ZoneId.of("America/New_York")
            )
        )
    }

    @Test
    fun meanSiderealTime() {
        assertEquals(128.7378734, Astro.meanSiderealTime(2446896.30625), 0.0001)
    }

    @Test
    fun apparentSiderealTime() {
        val jd = 2446895.5
        assertEquals(
            Astro.timeToAngle(13, 10, 46.1351),
            Astro.apparentSiderealTime(jd, -3.788, 23.44357),
            0.5
        )
    }

    @Test
    fun localMeanSiderealTime() {
        assertEquals(138.7378734, Astro.localMeanSidereal(2446896.30625, 10.0), 0.0001)
    }

    @Test
    fun localApparentSiderealTime() {
        val jd = 2446895.5
        assertEquals(
            Astro.timeToAngle(13, 10, 46.1351) - 15,
            Astro.localApparentSidereal(jd, -15.0, -3.788, 23.44357),
            0.5
        )
    }

    @Test
    fun hourAngle() {
        assertEquals(-2.0, Astro.hourAngle(10.0, -4.0, 8.0), 0.00001)
        assertEquals(2.0, Astro.hourAngle(10.0, 8.0), 0.00001)
    }

    @Test
    fun azimuth() {
        assertEquals(248.0337, Astro.azimuth(64.352133, 38.92139, -6.719892), 0.0001)
    }

    @Test
    fun altitude() {
        assertEquals(15.1249, Astro.altitude(64.352133, 38.92139, -6.719892), 0.0001)
    }

    @Test
    fun riseSetTransitTimes() {
        val times =
            Astro.riseSetTransitTimes(42.3333, -71.0833, 177.74208, -0.5667, 18.44092, 41.73129)
        assertEquals(0.51817 * 24, times.first, 0.001)
        assertEquals(0.81965 * 24, times.second, 0.001)
        assertEquals(0.12113 * 24, times.third, 0.001)
    }

    @Test
    fun accurateRiseSetTransitTimes() {
        val times = Astro.accurateRiseSetTransitTimes(
            42.3333, -71.0833, 177.74208, -0.5667, 56.0,
            Triple(18.04761, 18.44092, 18.82742), Triple(40.68021, 41.73129, 42.78204)
        )
        assertEquals(0.51766 * 24, times!!.first, 0.001)
        assertEquals(0.81980 * 24, times.second, 0.001)
        assertEquals(0.12130 * 24, times.third, 0.001)
    }

    @Test
    fun solarCoordinates() {
        val coords = Astro.solarCoordinates(2448908.5)
        assertEquals(-7.78507, coords.declination, 0.0001)
        assertEquals(-161.61917, coords.rightAscension, 0.0001)
    }

    @Test
    fun lunarCoordinates() {
        val coords = Astro.lunarCoordinates(2448724.5)
        assertEquals(13.768368, coords.declination, 0.001)
        assertEquals(134.688470, coords.rightAscension, 0.001)
    }

    data class RiseSetTransetTestInput(
        val date: LocalDate,
        val rise: LocalTime?,
        val transit: LocalTime?,
        val set: LocalTime?,
        val location: Coordinate = Coordinate(40.7128, -74.0060),
        val zone: String = "America/New_York"
    )

}