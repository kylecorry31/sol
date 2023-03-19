package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.science.astronomy.eclipse.EclipseType
import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.tests.assertDate
import com.kylecorry.sol.tests.assertDuration
import com.kylecorry.sol.tests.parametrized
import com.kylecorry.sol.time.Time.duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream

class AstronomyTest {

    // TODO: Verify sun events other than actual time

    @Test
    fun isSuperMoonTrue() {
        val date =
            ZonedDateTime.of(LocalDateTime.of(2021, Month.APRIL, 26, 12, 0), ZoneId.of("UTC"))
        val isSuperMoon = Astronomy.isSuperMoon(date)
        assertTrue(isSuperMoon)
    }

    @Test
    fun isSuperMoonNotFull() {
        val date =
            ZonedDateTime.of(LocalDateTime.of(2021, Month.APRIL, 21, 12, 0), ZoneId.of("UTC"))
        val isSuperMoon = Astronomy.isSuperMoon(date)
        assertFalse(isSuperMoon)
    }

    @Test
    fun isSuperMoonNotCloseEnough() {
        val date =
            ZonedDateTime.of(LocalDateTime.of(2021, Month.SEPTEMBER, 21, 12, 0), ZoneId.of("UTC"))
        val isSuperMoon = Astronomy.isSuperMoon(date)
        assertFalse(isSuperMoon)
    }

    @Test
    fun canGetMoonDistance() {
        val date = ZonedDateTime.of(LocalDateTime.of(1992, Month.APRIL, 12, 0, 0), ZoneId.of("UTC"))
        val distance = Astronomy.getMoonDistance(date)
        assertEquals(368409.06f, distance.distance, 0.1f)
    }

    @ParameterizedTest
    @MethodSource("providePartialLunarEclipses")
    fun canGetNextPartialLunarEclipse(
        latitude: Double,
        longitude: Double,
        date: String,
        start: String?,
        maximum: String?,
        end: String?,
        magnitude: Float,
        obscuration: Float
    ) {
        verifyEclipse(
            EclipseType.PartialLunar,
            latitude,
            longitude,
            date,
            start,
            maximum,
            end,
            magnitude,
            obscuration
        )
    }

    @Test
    fun canGetNextTotalEclipse() {
        val date = ZonedDateTime.of(LocalDateTime.of(2021, 8, 29, 0, 0), ZoneId.of("UTC"))
        val location = Coordinate(42.0, -70.0)

        val actual = Astronomy.getNextEclipse(date, location, EclipseType.TotalLunar)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, 5, 16, 3, 29), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, 5, 16, 4, 53), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertEquals(1.414f, actual.magnitude, 0.005f)
    }

    @ParameterizedTest
    @MethodSource("provideNextSolarEclipse")
    fun canGetNextSolarEclipse(
        latitude: Double,
        longitude: Double,
        date: String,
        start: String?,
        maximum: String?,
        end: String?,
        magnitude: Float,
        obscuration: Float
    ) {
        verifyEclipse(
            EclipseType.Solar,
            latitude,
            longitude,
            date,
            start,
            maximum,
            end,
            magnitude,
            obscuration
        )
    }

    @Test
    fun getSunEventsActual() {
        val cases = listOf(
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 12),
                LocalTime.of(6, 34),
                LocalTime.of(12, 52, 5),
                LocalTime.of(19, 9)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 22),
                LocalTime.of(6, 44),
                LocalTime.of(12, 48, 32),
                LocalTime.of(18, 52)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.MARCH, 21),
                LocalTime.of(6, 57),
                LocalTime.of(13, 2, 58),
                LocalTime.of(19, 10)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.DECEMBER, 21),
                LocalTime.of(7, 17),
                LocalTime.of(11, 54, 26),
                LocalTime.of(16, 32)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.DECEMBER, 21),
                LocalTime.of(7, 17),
                LocalTime.of(11, 54, 26),
                LocalTime.of(16, 32)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.JUNE, 21),
                LocalTime.of(5, 25),
                LocalTime.of(12, 57, 59),
                LocalTime.of(20, 31)
            ),
            RiseSetTransitTestInput( // UP ALL DAY
                LocalDate.of(2020, Month.JUNE, 4),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransitTestInput( // DOWN ALL DAY
                LocalDate.of(2020, Month.OCTOBER, 31),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.OCTOBER, 30),
                LocalTime.of(12, 41),
                LocalTime.of(12, 58),
                LocalTime.of(13, 13),
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2022, Month.OCTOBER, 29),
                LocalTime.of(7, 48),
                LocalTime.of(12, 44),
                LocalTime.of(17, 39),
                Coordinate(51.5, -0.13),
                "Europe/London"
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2022, Month.OCTOBER, 30),
                LocalTime.of(6, 50),
                LocalTime.of(11, 44),
                LocalTime.of(16, 37),
                Coordinate(51.5, -0.13),
                "Europe/London"
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2022, Month.DECEMBER, 27),
                LocalTime.of(8, 0),
                LocalTime.of(12, 3),
                LocalTime.of(16, 5),
                Coordinate(50.087778, 14.420556),
                "Europe/Prague"
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

            val actual = Astronomy.getSunEvents(date, case.location, SunTimesMode.Actual)
            assertRst(expected, actual)
        }
    }

    @Test
    fun getSunAltitude() {
        val ny = Coordinate(40.7128, -74.0060)
        parametrized(
            listOf(
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 8), 27.63f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 21, 58), -30.863646f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 22, 6, 51), 0.9f),
            )
        ) {
            val altitude = Astronomy.getSunAltitude(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny,
                true
            )
            assertEquals(it.second, altitude, 0.05f)
        }
    }

    @ParameterizedTest
    @MethodSource("provideSunAzimuth")
    fun getSunAzimuth(latitude: Double, longitude: Double, date: String, expected: Float) {
        val coordinate = Coordinate(latitude, longitude)
        val datetime = ZonedDateTime.parse(date)
        val azimuth = Astronomy.getSunAzimuth(datetime, coordinate)
        assertEquals(expected, azimuth.value, 0.5f)
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
            val sunset = Astronomy.getNextSunset(
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
            val sunrise = Astronomy.getNextSunrise(
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
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 12),
                LocalTime.of(0, 46),
                LocalTime.of(8, 34),
                LocalTime.of(16, 21)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 11),
                null,
                LocalTime.of(7, 39),
                LocalTime.of(15, 27)
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 24),
                LocalTime.of(15, 1),
                LocalTime.of(19, 38),
                null
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 11),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransitTestInput(
                LocalDate.of(2021, Month.FEBRUARY, 28),
                LocalTime.of(19, 29),
                LocalTime.of(1, 1),
                LocalTime.of(7, 35)
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

            val actual = Astronomy.getMoonEvents(date, case.location)
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
            val altitude = Astronomy.getMoonAltitude(
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
            val azimuth = Astronomy.getMoonAzimuth(
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
            val moonset = Astronomy.getNextMoonset(
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
            val moonrise = Astronomy.getNextMoonrise(
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
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 2, 14, 58))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.Full, 100f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 9, 13, 48))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.ThirdQuarter, 50f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 16, 5, 35))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.New, 0f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 24, 5, 29))),
            tolerance
        )

        // Intermediate phases
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaxingCrescent, 23f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 29, 12, 0))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaxingGibbous, 79f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 5, 12, 0))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaningGibbous, 79f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 13, 12, 0))),
            tolerance
        )
        assertMoonPhases(
            MoonPhase(MoonTruePhase.WaningCrescent, 28f),
            Astronomy.getMoonPhase(getDate(LocalDateTime.of(2020, Month.MARCH, 18, 12, 0))),
            tolerance
        )
    }

    @Test
    fun isSunUp() {
        val ny = Coordinate(40.7128, -74.0060)
        val zoneNy = ZoneId.of("America/New_York")
        assertTrue(
            Astronomy.isSunUp(
                ZonedDateTime.of(
                    LocalDateTime.of(
                        2020,
                        Month.SEPTEMBER,
                        13,
                        12,
                        0
                    ), zoneNy
                ), ny
            )
        )
        assertFalse(
            Astronomy.isSunUp(
                ZonedDateTime.of(
                    LocalDateTime.of(
                        2020,
                        Month.SEPTEMBER,
                        13,
                        23,
                        0
                    ), zoneNy
                ), ny
            )
        )
    }

    @Test
    fun isMoonUp() {
        val ny = Coordinate(40.7128, -74.0060)
        val zoneNy = ZoneId.of("America/New_York")
        assertTrue(
            Astronomy.isMoonUp(
                ZonedDateTime.of(
                    LocalDateTime.of(
                        2020,
                        Month.SEPTEMBER,
                        13,
                        9,
                        8
                    ), zoneNy
                ), ny
            )
        )
        assertFalse(
            Astronomy.isMoonUp(
                ZonedDateTime.of(
                    LocalDateTime.of(
                        2020,
                        Month.SEPTEMBER,
                        13,
                        21,
                        58
                    ), zoneNy
                ), ny
            )
        )
    }

    @Test
    fun getMeteorShower() {
        val location = Coordinate(40.7128, -74.0060)

        listOf<Pair<MeteorShower?, LocalDate>>(
            MeteorShower.Quadrantids to LocalDate.of(2022, 1, 3),
            MeteorShower.Lyrids to LocalDate.of(2022, 4, 22),
            MeteorShower.EtaAquariids to LocalDate.of(2022, 5, 6),
            MeteorShower.DeltaAquariids to LocalDate.of(2022, 7, 30),
            MeteorShower.Perseids to LocalDate.of(2022, 8, 12),
            MeteorShower.Orionids to LocalDate.of(2022, 10, 21),
            MeteorShower.Leonids to LocalDate.of(2022, 11, 18),
            MeteorShower.Geminids to LocalDate.of(2022, 12, 14),
            MeteorShower.Ursids to LocalDate.of(2022, 12, 22),
            null to LocalDate.of(2022, 1, 1)
        ).forEach {
            val shower = Astronomy.getMeteorShower(
                location,
                getDate(LocalDateTime.of(it.second, LocalTime.MIN))
            )
            assertEquals(it.first, shower?.shower)
        }
    }

    @Test
    fun getAstronomicalSeasonNorthern() {
        val location = Coordinate(40.7128, -74.0060)

        listOf<Pair<Season, LocalDate>>(
            Season.Winter to LocalDate.of(2021, 1, 1),
            Season.Winter to LocalDate.of(2021, 3, 19),
            Season.Spring to LocalDate.of(2021, 3, 20),
            Season.Spring to LocalDate.of(2021, 6, 19),
            Season.Summer to LocalDate.of(2021, 6, 20),
            Season.Summer to LocalDate.of(2021, 9, 21),
            Season.Fall to LocalDate.of(2021, 9, 22),
            Season.Fall to LocalDate.of(2021, 12, 20),
            Season.Winter to LocalDate.of(2021, 12, 21),
            Season.Winter to LocalDate.of(2021, 12, 31),
        ).forEach {
            val season = Astronomy.getSeason(
                location,
                getDate(LocalDateTime.of(it.second, LocalTime.MAX))
            )
            assertEquals(it.first, season)
        }
    }

    @Test
    fun getAstronomicalSeasonSouthern() {
        val location = Coordinate(-40.7128, -74.0060)

        listOf<Pair<Season, LocalDate>>(
            Season.Summer to LocalDate.of(2021, 1, 1),
            Season.Summer to LocalDate.of(2021, 3, 19),
            Season.Fall to LocalDate.of(2021, 3, 20),
            Season.Fall to LocalDate.of(2021, 6, 19),
            Season.Winter to LocalDate.of(2021, 6, 20),
            Season.Winter to LocalDate.of(2021, 9, 21),
            Season.Spring to LocalDate.of(2021, 9, 22),
            Season.Spring to LocalDate.of(2021, 12, 20),
            Season.Summer to LocalDate.of(2021, 12, 21),
            Season.Summer to LocalDate.of(2021, 12, 31),
        ).forEach {
            val season = Astronomy.getSeason(
                location,
                getDate(LocalDateTime.of(it.second, LocalTime.MAX))
            )
            assertEquals(it.first, season)
        }
    }

    @Test
    fun defaultGetSunEvents() {
        val expected = RiseSetTransitTestInput(
            LocalDate.of(2020, Month.SEPTEMBER, 12),
            LocalTime.of(6, 34),
            LocalTime.of(12, 52),
            LocalTime.of(19, 9)
        )

        val date = ZonedDateTime.of(
            expected.date,
            LocalTime.of(12, 0),
            ZoneId.of(expected.zone)
        )

        val e = RiseSetTransitTimes(
            if (expected.rise != null) date.withHour(expected.rise.hour)
                .withMinute(expected.rise.minute) else null,
            if (expected.transit != null) date.withHour(expected.transit.hour)
                .withMinute(expected.transit.minute) else null,
            if (expected.set != null) date.withHour(expected.set.hour)
                .withMinute(expected.set.minute) else null
        )

        val actual = Astronomy.getSunEvents(date, expected.location)
        assertRst(e, actual)
    }

    @ParameterizedTest
    @MethodSource("provideDayLengths")
    fun getDaylightLength(date: ZonedDateTime, location: Coordinate, expected: Duration) {
        val length = Astronomy.getDaylightLength(date, location)
        assertDuration(expected, length, Duration.ofSeconds(30))
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

    data class RiseSetTransitTestInput(
        val date: LocalDate,
        val rise: LocalTime?,
        val transit: LocalTime?,
        val set: LocalTime?,
        val location: Coordinate = Coordinate(40.7128, -74.0060),
        val zone: String = "America/New_York"
    )

    private fun verifyEclipse(
        type: EclipseType,
        latitude: Double,
        longitude: Double,
        date: String,
        start: String?,
        maximum: String?,
        end: String?,
        magnitude: Float,
        obscuration: Float
    ) {
        val datetime = ZonedDateTime.parse(date)
        val location = Coordinate(latitude, longitude)

        val actual = Astronomy.getNextEclipse(datetime, location, type)

        if (start == null || end == null) {
            assertNull(actual)
            return
        }

        assertNotNull(actual)

        val zone = ZonedDateTime.parse(start).zone

        assertDate(
            ZonedDateTime.parse(start),
            actual!!.start.atZone(zone),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.parse(maximum),
            actual.maximum.atZone(zone),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.parse(end),
            actual.end.atZone(zone),
            Duration.ofMinutes(2)
        )

        assertEquals(magnitude, actual.magnitude, 0.005f)
        assertEquals(obscuration, actual.obscuration, 0.005f)
    }


    companion object {

        @JvmStatic
        fun providePartialLunarEclipses(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    42.0,
                    -70.0,
                    "2021-08-29T00:00:00Z",
                    "2021-11-19T07:18:00Z",
                    "2021-11-19T09:03:00Z",
                    "2021-11-19T10:47:00Z",
                    0.974f,
                    0.991f
                ),
                Arguments.of(
                    42.0,
                    -70.0,
                    "2021-12-01T10:47:00Z",
                    "2022-05-16T02:28:00Z",
                    "2022-05-16T04:11:00Z",
                    "2022-05-16T05:54:00Z",
                    1.413f,
                    1f
                ),
                Arguments.of(
                    42.0,
                    -70.0,
                    "2023-01-01T00:00:00Z",
                    "2024-09-18T02:13:00Z",
                    "2024-09-18T02:44:00Z",
                    "2024-09-18T03:15:00Z",
                    0.078f,
                    0.035f
                ),
                Arguments.of(
                    42.0,
                    -70.0,
                    "2024-09-19T00:00:00Z",
                    "2025-03-14T05:09:00Z",
                    "2025-03-14T06:58:00Z",
                    "2025-03-14T08:47:00Z",
                    1.178f,
                    1f
                ),
                Arguments.of(
                    42.0,
                    70.0,
                    "2021-12-01T10:47:00Z",
                    "2022-11-08T09:09:00Z",
                    "2022-11-08T10:59:00Z",
                    "2022-11-08T12:49:00Z",
                    1.359f,
                    1f
                ),

                // At start
                Arguments.of(
                    42.0,
                    -70.0,
                    "2021-11-19T07:18:00Z",
                    "2021-11-19T07:18:00Z",
                    "2021-11-19T09:03:00Z",
                    "2021-11-19T10:47:00Z",
                    0.974f,
                    0.991f
                ),

                // At middle
                Arguments.of(
                    42.0,
                    -70.0,
                    "2021-11-19T09:03:00Z",
                    "2021-11-19T07:18:00Z",
                    "2021-11-19T09:03:00Z",
                    "2021-11-19T10:47:00Z",
                    0.974f,
                    0.991f
                ),

                // At end
                Arguments.of(
                    42.0,
                    -70.0,
                    "2021-11-19T10:47:00Z",
                    "2022-05-16T02:28:00Z",
                    "2022-05-16T04:11:00Z",
                    "2022-05-16T05:54:00Z",
                    1.413f,
                    1f
                ),
            )
        }

        @JvmStatic
        fun provideNextSolarEclipse(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    24.61167,
                    143.36167,
                    "2009-07-22T00:00:00Z",
                    "2009-07-22T00:58:00Z",
                    "2009-07-22T02:19:00Z",
                    "2009-07-22T03:42:00Z",
                    0.7456f,
                    0.69520104f
                ),
                Arguments.of(
                    42.0,
                    -70.0,
                    "2023-01-01T00:00:00Z",
                    "2023-10-14T16:21:00Z",
                    "2023-10-14T17:28:00Z",
                    "2023-10-14T18:38:00Z",
                    0.2913301f,
                    0.17762049f
                ),
                Arguments.of(
                    42.0,
                    -70.0,
                    "2023-10-15T00:00:00Z",
                    "2024-04-08T18:18:00Z",
                    "2024-04-08T19:30:00Z",
                    "2024-04-08T20:41:00Z",
                    0.92334f,
                    0.91496944f
                ),
                Arguments.of(
                    42.0,
                    -70.0,
                    "2024-04-09T00:00:00Z",
                    "2025-03-29T10:29:00Z",
                    "2025-03-29T10:29:00Z",
                    "2025-03-29T11:08:00Z",
                    0.6743827f,
                    0.6000332f
                ),
                Arguments.of(
                    25.28,
                    -104.12,
                    "2023-10-15T00:00:00Z",
                    "2024-04-08T17:03:00Z",
                    "2024-04-08T18:23:00Z",
                    "2024-04-08T19:46:00Z",
                    0.94278395f,
                    0.94278395f
                ), // This should be a total eclipse

                // No eclipses
                Arguments.of(40.0, 120.0, "2023-01-01T00:00:00Z", null, null, null, 0.0f, 0.0f),
                Arguments.of(40.0, 120.0, "2023-10-15T00:00:00Z", null, null, null, 0.0f, 0.0f),

                // Search starts at start of eclipse
                Arguments.of(
                    42.0,
                    -70.0,
                    "2023-10-14T16:21:00Z",
                    "2023-10-14T16:21:00Z",
                    "2023-10-14T17:28:00Z",
                    "2023-10-14T18:38:00Z",
                    0.29133186f,
                    0.17762049f
                ),
                // Search starts during eclipse
                Arguments.of(
                    42.0,
                    -70.0,
                    "2023-10-14T18:00:00Z",
                    "2023-10-14T16:21:00Z",
                    "2023-10-14T17:28:00Z",
                    "2023-10-14T18:38:00Z",
                    0.29133186f,
                    0.17762049f
                ),
                // Search starts at end of eclipse
                Arguments.of(
                    42.0,
                    -70.0,
                    "2023-10-14T18:38:00Z",
                    "2024-04-08T18:18:00Z",
                    "2024-04-08T19:30:00Z",
                    "2024-04-08T20:41:00Z",
                    0.92334f,
                    0.91496944f
                )
            )
        }

        @JvmStatic
        fun provideSunAzimuth(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(40.7128, -74.006, "2020-09-13T09:08:00-04", 111.0f),
                Arguments.of(40.7128, -74.006, "2020-09-13T21:58:00-04", 307.0f),
                Arguments.of(-21.48575, -55.54686, "2023-03-17T13:45:00-04", 302.18f)
            )
        }

        @JvmStatic
        fun provideDayLengths(): Stream<Arguments> {
            val ny = Coordinate(40.7128, -74.0060)
            val zoneNy = ZoneId.of("America/New_York")
            val greenland = Coordinate(76.7667, -18.6667)
            val zoneGreenland = ZoneId.of("America/Danmarkshavn")
            return Stream.of(
                Arguments.of(
                    ZonedDateTime.of(LocalDate.of(2021, 4, 3), LocalTime.MIN, zoneNy),
                    ny,
                    duration(12, 47)
                ),
                Arguments.of(
                    ZonedDateTime.of(LocalDate.of(2021, 3, 13), LocalTime.MIN, zoneNy),
                    ny,
                    duration(11, 50)
                ),
                Arguments.of(
                    ZonedDateTime.of(LocalDate.of(2021, 3, 14), LocalTime.MIN, zoneNy),
                    ny,
                    duration(11, 53)
                ),
                Arguments.of(
                    ZonedDateTime.of(
                        LocalDate.of(2021, 1, 10),
                        LocalTime.MIN,
                        zoneGreenland
                    ), greenland, duration(0)
                ),
                Arguments.of(
                    ZonedDateTime.of(
                        LocalDate.of(2021, 2, 17),
                        LocalTime.MIN,
                        zoneGreenland
                    ), greenland, duration(4, 38)
                ),
                Arguments.of(
                    ZonedDateTime.of(
                        LocalDate.of(2021, 4, 22),
                        LocalTime.MIN,
                        zoneGreenland
                    ), greenland, duration(22, 42)
                ),
                Arguments.of(
                    ZonedDateTime.of(
                        LocalDate.of(2021, 5, 22),
                        LocalTime.MIN,
                        zoneGreenland
                    ), greenland, duration(24)
                ),
            )
        }
    }
}