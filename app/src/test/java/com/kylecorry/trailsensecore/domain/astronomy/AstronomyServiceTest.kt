package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.tests.assertDate
import com.kylecorry.trailsensecore.tests.parametrized
import org.junit.Test

import org.junit.Assert.*
import java.time.*

class AstronomyServiceTest {

    private val service = AstronomyService()

    @Test
    fun getSunEventsActual() {
        val cases = listOf(
            AstroTest.RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 12),
                LocalTime.of(6, 34),
                LocalTime.of(12, 52),
                LocalTime.of(19, 9)
            ),
            AstroTest.RiseSetTransetTestInput( // UP ALL DAY
                LocalDate.of(2020, Month.JUNE, 4),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            AstroTest.RiseSetTransetTestInput( // DOWN ALL DAY
                LocalDate.of(2020, Month.OCTOBER, 31),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            AstroTest.RiseSetTransetTestInput(
                LocalDate.of(2020, Month.OCTOBER, 30),
                LocalTime.of(12, 41),
                LocalTime.of(12, 58),
                LocalTime.of(13, 13),
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            )
        )

        for (case in cases) {
            val date = ZonedDateTime.of(
                case.date,
                LocalTime.of(12, 0),
                ZoneId.of(case.zone)
            )

            val expected = RiseSetTransitTimes(
                if (case.rise != null) date.withHour(case.rise.hour)
                    .withMinute(case.rise.minute) else null,
                if (case.transit != null) date.withHour(case.transit.hour)
                    .withMinute(case.transit.minute) else null,
                if (case.set != null) date.withHour(case.set.hour)
                    .withMinute(case.set.minute) else null
            )

            val actual = service.getSunEvents(date, case.location, SunTimesMode.Actual)
            assertRst(expected, actual)
        }
    }

    @Test
    fun getSunAltitude() {
        val ny = Coordinate(40.7128, -74.0060)
        parametrized(
            listOf(
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 8), 28f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 21, 58), -31f),
            )
        ) {
            val altitude = service.getSunAltitude(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny
            )
            assertEquals(it.second, altitude, 0.5f)
        }
    }

    @Test
    fun getSunAzimuth() {
        val ny = Coordinate(40.7128, -74.0060)
        parametrized(
            listOf(
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 8), 111f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 21, 58), 307f),
            )
        ) {
            val azimuth =
                service.getSunAzimuth(ZonedDateTime.of(it.first, ZoneId.of("America/New_York")), ny)
            assertEquals(it.second, azimuth.value, 0.5f)
        }
    }

    @Test
    fun getNextSunset() {
        val ny = Coordinate(40.7128, -74.0060)
        val gl = Coordinate(76.7667, -18.6667)
        parametrized(
            listOf<List<Any?>>(
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 19, 7)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 20, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 14, 19, 5)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.JUNE, 4, 12, 0),
                    gl,
                    "America/Danmarkshavn",
                    null
                ),
                listOf(
                    LocalDateTime.of(2020, Month.AUGUST, 19, 12, 0),
                    gl,
                    "America/Danmarkshavn",
                    LocalDateTime.of(2020, Month.AUGUST, 20, 0, 56)
                )
            )
        ) {
            val sunset = service.getNextSunset(
                ZonedDateTime.of(
                    it[0] as LocalDateTime,
                    ZoneId.of(it[2] as String)
                ), it[1] as Coordinate
            )
            val expected = if (it[3] == null) null else ZonedDateTime.of(
                it[3] as LocalDateTime,
                ZoneId.of(it[2] as String)
            )
            assertDate(expected, sunset, Duration.ofMinutes(1))
        }
    }

    @Test
    fun getNextSunrise() {
        val ny = Coordinate(40.7128, -74.0060)
        val gl = Coordinate(76.7667, -18.6667)
        parametrized(
            listOf<List<Any?>>(
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 6, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 6, 35)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 20, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 14, 6, 36)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.JUNE, 4, 12, 0),
                    gl,
                    "America/Danmarkshavn",
                    null
                ),
                listOf(
                    LocalDateTime.of(2020, Month.AUGUST, 19, 12, 0),
                    gl,
                    "America/Danmarkshavn",
                    LocalDateTime.of(2020, Month.AUGUST, 20, 1, 41)
                )
            )
        ) {
            val sunrise = service.getNextSunrise(
                ZonedDateTime.of(
                    it[0] as LocalDateTime,
                    ZoneId.of(it[2] as String)
                ), it[1] as Coordinate
            )
            val expected = if (it[3] == null) null else ZonedDateTime.of(
                it[3] as LocalDateTime,
                ZoneId.of(it[2] as String)
            )
            assertDate(expected, sunrise, Duration.ofMinutes(1))
        }
    }

    @Test
    fun getMoonEvents() {
        val cases = listOf(
            AstroTest.RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 12),
                LocalTime.of(0, 46),
                LocalTime.of(8, 34),
                LocalTime.of(16, 21)
            ),
            AstroTest.RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 11),
                null,
                LocalTime.of(7, 39),
                LocalTime.of(15, 27)
            ),
            AstroTest.RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 24),
                LocalTime.of(15, 1),
                LocalTime.of(19, 38),
                null
            ),
            AstroTest.RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 11),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            )
        )

        for (case in cases) {
            val date = ZonedDateTime.of(
                case.date,
                LocalTime.of(10, 0),
                ZoneId.of(case.zone)
            )

            val expected = RiseSetTransitTimes(
                if (case.rise != null) date.withHour(case.rise.hour)
                    .withMinute(case.rise.minute) else null,
                if (case.transit != null) date.withHour(case.transit.hour)
                    .withMinute(case.transit.minute) else null,
                if (case.set != null) date.withHour(case.set.hour)
                    .withMinute(case.set.minute) else null
            )

            val actual = service.getMoonEvents(date, case.location)
            assertRst(expected, actual)
        }
    }

    @Test
    fun getMoonAltitude() {
        val ny = Coordinate(40.7128, -74.0060)
        parametrized(
            listOf(
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 8), 72f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 21, 58), -28f),
            )
        ) {
            val altitude = service.getMoonAltitude(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny
            )
            assertEquals(it.second, altitude, 0.8f)
        }
    }

    @Test
    fun getMoonAzimuth() {
        val ny = Coordinate(40.7128, -74.0060)
        parametrized(
            listOf(
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 8), 165f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 21, 58), 360f),
            )
        ) {
            val azimuth = service.getMoonAzimuth(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny
            )
            assertEquals(it.second, azimuth.value, 2f)
        }
    }

    @Test
    fun getNextMoonset() {
        val ny = Coordinate(40.7128, -74.0060)
        val gl = Coordinate(76.7667, -18.6667)
        parametrized(
            listOf<List<Any?>>(
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 17, 10)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 20, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 14, 17, 53)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 8, 12, 0),
                    gl,
                    "America/Danmarkshavn",
                    null
                )
            )
        ) {
            val moonset = service.getNextMoonset(
                ZonedDateTime.of(
                    it[0] as LocalDateTime,
                    ZoneId.of(it[2] as String)
                ), it[1] as Coordinate
            )
            val expected = if (it[3] == null) null else ZonedDateTime.of(
                it[3] as LocalDateTime,
                ZoneId.of(it[2] as String)
            )
            assertDate(expected, moonset, Duration.ofMinutes(1))
        }
    }

    @Test
    fun getNextMoonrise() {
        val ny = Coordinate(40.7128, -74.0060)
        val gl = Coordinate(76.7667, -18.6667)
        parametrized(
            listOf<List<Any?>>(
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 0, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 1, 45)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 13, 2, 0),
                    ny,
                    "America/New_York",
                    LocalDateTime.of(2020, Month.SEPTEMBER, 14, 2, 51)
                ),
                listOf(
                    LocalDateTime.of(2020, Month.SEPTEMBER, 8, 12, 0),
                    gl,
                    "America/Danmarkshavn",
                    null
                )
            )
        ) {
            val moonrise = service.getNextMoonrise(
                ZonedDateTime.of(
                    it[0] as LocalDateTime,
                    ZoneId.of(it[2] as String)
                ), it[1] as Coordinate
            )
            val expected = if (it[3] == null) null else ZonedDateTime.of(
                it[3] as LocalDateTime,
                ZoneId.of(it[2] as String)
            )
            assertDate(expected, moonrise, Duration.ofMinutes(1))
        }
    }

    @Test
    fun getMoonPhase() {
        val tolerance = 0.5f

        // Main phases
        assertMoonPhases(
            MoonPhase(MoonTruePhase.FirstQuarter, 50f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 2, 14, 58))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.Full, 100f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 9, 13, 48))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.ThirdQuarter, 50f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 16, 5, 35))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.New, 0f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 24, 5, 29))),
            tolerance
        )

        // Intermediate phases
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaxingCrescent, 23f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 29, 12, 0))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaxingGibbous, 79f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 5, 12, 0))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaningGibbous, 79f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 13, 12, 0))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaningCrescent, 28f),
            service.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 18, 12, 0))),
            tolerance
        )
    }


    private fun getDate(time: LocalDateTime): ZonedDateTime {
        return time.atZone(ZoneId.of("America/New_York"))
    }

    private fun assertMoonPhases(expected: MoonPhase, actual: MoonPhase, tolerance: Float) {
        assertEquals(expected.phase, actual.phase)
        assertEquals(expected.illumination, actual.illumination, tolerance)
    }

    private fun assertRst(
        expected: RiseSetTransitTimes,
        actual: RiseSetTransitTimes,
        maxDifference: Duration = Duration.ofMinutes(1)
    ) {
        assertDate(expected.rise, actual.rise, maxDifference)
        assertDate(expected.transit, actual.transit, maxDifference)
        assertDate(expected.set, actual.set, maxDifference)
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