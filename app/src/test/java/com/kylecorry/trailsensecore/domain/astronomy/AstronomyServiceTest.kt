package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.domain.time.Season
import com.kylecorry.trailsensecore.domain.time.duration
import com.kylecorry.trailsensecore.tests.assertDate
import com.kylecorry.trailsensecore.tests.assertDuration
import com.kylecorry.trailsensecore.tests.parametrized
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream

class AstronomyServiceTest {

    private val service = AstronomyService()

    // TODO: Verify sun events other than actual time

    @Test
    fun getSunEventsActual() {
        val cases = listOf(
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 12),
                LocalTime.of(6, 34),
                LocalTime.of(12, 52, 5),
                LocalTime.of(19, 9)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 22),
                LocalTime.of(6, 44),
                LocalTime.of(12, 48, 32),
                LocalTime.of(18, 52)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.MARCH, 21),
                LocalTime.of(6, 57),
                LocalTime.of(13, 2, 58),
                LocalTime.of(19, 10)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.DECEMBER, 21),
                LocalTime.of(7, 17),
                LocalTime.of(11, 54, 26),
                LocalTime.of(16, 32)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.DECEMBER, 21),
                LocalTime.of(7, 17),
                LocalTime.of(11, 54, 26),
                LocalTime.of(16, 32)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.JUNE, 21),
                LocalTime.of(5, 25),
                LocalTime.of(12, 57, 59),
                LocalTime.of(20, 31)
            ),
            RiseSetTransetTestInput( // UP ALL DAY
                LocalDate.of(2020, Month.JUNE, 4),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransetTestInput( // DOWN ALL DAY
                LocalDate.of(2020, Month.OCTOBER, 31),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransetTestInput(
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
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 9, 8), 27.63f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 21, 58), -30.8f),
                Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 22, 6, 51), 0.9f),
            )
        ) {
            val altitude = service.getSunAltitude(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny,
                true
            )
            assertEquals(it.second, altitude, 0.05f)
        }
    }

    @Test
    fun getBestSolarPanelPositionForDayNorthern(){
        val ny = Coordinate(40.7128, -74.0060)

        parametrized(
            listOf(
                Triple(LocalDateTime.of(2020, Month.JULY, 21, 0, 0), 179f, 22.5f),
                Triple(LocalDateTime.of(2020, Month.NOVEMBER, 22, 0, 0), 180f, 65.7f),
                Triple(LocalDateTime.of(2020, Month.NOVEMBER, 22, 12, 30), 180f, 65.7f),
            )
        ) {
            val position = service.getBestSolarPanelPositionForDay(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny
            )
            assertEquals(it.second, position.bearing.value, 0.5f)
            assertEquals(it.third, position.tilt, 0.5f)
        }
    }

    @Test
    fun getBestSolarPanelPositionForDaySouthern(){
        val ny = Coordinate(-40.7128, -74.0060)
        parametrized(
            listOf(
                Triple(LocalDateTime.of(2020, Month.JULY, 21, 0, 0), 360f, 65.68f),
                Triple(LocalDateTime.of(2020, Month.NOVEMBER, 22, 0, 0), 360f, 22.54f),
                Triple(LocalDateTime.of(2020, Month.NOVEMBER, 22, 12, 30), 360f, 22.54f),
            )
        ) {
            val position = service.getBestSolarPanelPositionForDay(
                ZonedDateTime.of(it.first, ZoneId.of("America/New_York")),
                ny
            )
            assertEquals(it.second, position.bearing.value, 0.5f)
            assertEquals(it.third, position.tilt, 0.05f)
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
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 12),
                LocalTime.of(0, 46),
                LocalTime.of(8, 34),
                LocalTime.of(16, 21)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 11),
                null,
                LocalTime.of(7, 39),
                LocalTime.of(15, 27)
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 24),
                LocalTime.of(15, 1),
                LocalTime.of(19, 38),
                null
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2020, Month.SEPTEMBER, 11),
                null,
                null,
                null,
                Coordinate(76.7667, -18.6667),
                "America/Danmarkshavn"
            ),
            RiseSetTransetTestInput(
                LocalDate.of(2021, Month.FEBRUARY, 28),
                LocalTime.of(19, 28),
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

    @Test
    fun isSunUp() {
        val ny = Coordinate(40.7128, -74.0060)
        val zoneNy = ZoneId.of("America/New_York")
        assertTrue(
            service.isSunUp(
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
            service.isSunUp(
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
            service.isMoonUp(
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
            service.isMoonUp(
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
    fun getMeteorShower(){
        val service = AstronomyService()
        val location = Coordinate(40.7128, -74.0060)

        listOf<Pair<MeteorShower?, LocalDate>>(
            MeteorShower.Quadrantids to LocalDate.of(2021, 1, 3),
            MeteorShower.Lyrids to LocalDate.of(2021, 4, 22),
            MeteorShower.EtaAquariids to LocalDate.of(2021, 5, 6),
            MeteorShower.DeltaAquariids to LocalDate.of(2021, 7, 30),
            MeteorShower.Perseids to LocalDate.of(2021, 8, 12),
            MeteorShower.Orionids to LocalDate.of(2021, 10, 20),
            MeteorShower.Leonids to LocalDate.of(2021, 11, 18),
            MeteorShower.Geminids to LocalDate.of(2021, 12, 14),
            MeteorShower.Ursids to LocalDate.of(2021, 12, 21),
            null to LocalDate.of(2021, 1, 1)
        ).forEach {
            val shower = service.getMeteorShower(location, getDate(LocalDateTime.of(it.second, LocalTime.MIN)))
            assertEquals(it.first, shower?.shower)
        }
    }

    @Test
    fun getAstronomicalSeasonNorthern(){
        val service = AstronomyService()
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
            val season = service.getAstronomicalSeason(location, getDate(LocalDateTime.of(it.second, LocalTime.MAX)))
            assertEquals(it.first, season)
        }
    }

    @Test
    fun getAstronomicalSeasonSouthern(){
        val service = AstronomyService()
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
            val season = service.getAstronomicalSeason(location, getDate(LocalDateTime.of(it.second, LocalTime.MAX)))
            assertEquals(it.first, season)
        }
    }

    @Test
    fun defaultGetSunEvents() {
        val expected = RiseSetTransetTestInput(
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

        val actual = service.getSunEvents(date, expected.location)
        assertRst(e, actual)
    }

    @ParameterizedTest
    @MethodSource("provideDayLengths")
    fun getDaylightLength(date: ZonedDateTime, location: Coordinate, expected: Duration){
        val length = service.getDaylightLength(date, location)
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

    data class RiseSetTransetTestInput(
        val date: LocalDate,
        val rise: LocalTime?,
        val transit: LocalTime?,
        val set: LocalTime?,
            val location: Coordinate = Coordinate(40.7128, -74.0060),
        val zone: String = "America/New_York"
    )


    companion object {
        @JvmStatic
        fun provideDayLengths(): Stream<Arguments> {
            val ny = Coordinate(40.7128, -74.0060)
            val zoneNy = ZoneId.of("America/New_York")
            val greenland = Coordinate(76.7667, -18.6667)
            val zoneGreenland = ZoneId.of("America/Danmarkshavn")
            return Stream.of(
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 4, 3), LocalTime.MIN, zoneNy), ny, duration(12, 47)),
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 3, 13), LocalTime.MIN, zoneNy), ny, duration(11, 50)),
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 3, 14), LocalTime.MIN, zoneNy), ny, duration(11, 53)),
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 1, 10), LocalTime.MIN, zoneGreenland), greenland, duration(0)),
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 2, 17), LocalTime.MIN, zoneGreenland), greenland, duration(4, 37)),
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 4, 22), LocalTime.MIN, zoneGreenland), greenland, duration(22, 42)),
                Arguments.of(ZonedDateTime.of(LocalDate.of(2021, 5, 22), LocalTime.MIN, zoneGreenland), greenland, duration(24)),
            )
        }
    }
}