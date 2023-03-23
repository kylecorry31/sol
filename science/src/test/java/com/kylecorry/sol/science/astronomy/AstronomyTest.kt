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
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
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

    @ParameterizedTest
    @MethodSource("provideTotalLunarEclipses")
    fun canGetNextTotalEclipse(
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
            EclipseType.TotalLunar,
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

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-12T12:00:00-04, 2020-09-12T06:34:00-04, 2020-09-12T12:52:00-04, 2020-09-12T19:09:00-04",
        "40.7128, -74.0060, 2020-09-22T12:00:00-04, 2020-09-22T06:44:00-04, 2020-09-22T12:48:00-04, 2020-09-22T18:52:00-04",
        "40.7128, -74.0060, 2020-03-21T12:00:00-04, 2020-03-21T06:57:00-04, 2020-03-21T13:02:00-04, 2020-03-21T19:10:00-04",
        "40.7128, -74.0060, 2020-12-21T12:00:00-05, 2020-12-21T07:17:00-05, 2020-12-21T11:54:00-05, 2020-12-21T16:32:00-05",
        "40.7128, -74.0060, 2020-06-21T12:00:00-04, 2020-06-21T05:25:00-04, 2020-06-21T12:58:00-04, 2020-06-21T20:31:00-04",
        "76.7667, -18.6667, 2020-06-04T12:00:00Z, , ,",
        "76.7667, -18.6667, 2020-10-31T12:00:00Z, , ,",
        "76.7667, -18.6667, 2020-10-30T12:00:00Z, 2020-10-30T12:41:00Z, 2020-10-30T12:58:00Z, 2020-10-30T13:13:00Z",
        "51.5, -0.13, 2020-10-29T12:00:00+01, 2020-10-29T07:48:00+01, 2020-10-29T12:44:00+01, 2020-10-29T17:39:00+01",
        "51.5, -0.13, 2020-10-30T12:00:00Z, 2020-10-30T06:50:00Z, 2020-10-30T11:44:00Z, 2020-10-30T16:37:00Z",
        "50.087778, 14.420556, 2022-12-27T12:00:00+01, 2022-12-27T08:00:00+01, 2022-12-27T12:03:00+01, 2022-12-27T16:05:00+01",
    )
    fun getSunEventsActual(
        latitude: Double,
        longitude: Double,
        date: String,
        rise: String?,
        transit: String?,
        set: String?
    ) {
        val coordinate = Coordinate(latitude, longitude)
        val lookupDate = ZonedDateTime.parse(date)

        val sunEvents = Astronomy.getSunEvents(lookupDate, coordinate, SunTimesMode.Actual)

        if (rise != null) {
            assertDate(
                ZonedDateTime.parse(rise),
                sunEvents.rise,
                Duration.ofMinutes(2)
            )
        } else {
            assertNull(sunEvents.rise)
        }

        if (transit != null) {
            assertDate(
                ZonedDateTime.parse(transit),
                sunEvents.transit,
                Duration.ofMinutes(2)
            )
        } else {
            assertNull(sunEvents.transit)
        }

        if (set != null) {
            assertDate(
                ZonedDateTime.parse(set),
                sunEvents.set,
                Duration.ofMinutes(2)
            )
        } else {
            assertNull(sunEvents.set)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T09:08:00-04, 27.63",
        "40.7128, -74.0060, 2020-09-13T21:58:00-04, -30.863646",
        "40.7128, -74.0060, 2020-09-22T06:51:00-04, 0.9",
    )
    fun getSunAltitude(latitude: Double, longitude: Double, time: String, altitude: Float) {
        val coordinate = Coordinate(latitude, longitude)
        val datetime = ZonedDateTime.parse(time)

        val actual = Astronomy.getSunAltitude(datetime, coordinate, true)

        assertEquals(altitude, actual, 0.05f)
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.006, 2020-09-13T09:08:00-04, 111.0",
        "40.7128, -74.006, 2020-09-13T21:58:00-04, 307.0",
        "-21.48575, -55.54686, 2023-03-17T13:45:00-04, 302.18"
    )
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

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T09:08:00-04, 72.0",
        "40.7128, -74.0060, 2020-09-13T21:58:00-04, -28.0"
    )
    fun getMoonAltitude(latitude: Double, longitude: Double, time: String, altitude: Float) {
        val actual = Astronomy.getMoonAltitude(
            ZonedDateTime.parse(time),
            Coordinate(latitude, longitude),
            true
        )
        assertEquals(altitude, actual, 0.8f)
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T09:08:00-04, 165.0",
        "40.7128, -74.0060, 2020-09-13T21:58:00-04, 360.0"
    )
    fun getMoonAzimuth(latitude: Double, longitude: Double, time: String, azimuth: Float) {
        val actual = Astronomy.getMoonAzimuth(
            ZonedDateTime.parse(time),
            Coordinate(latitude, longitude)
        )
        assertEquals(azimuth, actual.value, 2f)
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T09:00:00-04, 2020-09-13T17:10:00-04",
        "40.7128, -74.0060, 2020-09-13T20:00:00-04, 2020-09-14T17:53:00-04",
        "76.7667, -18.6667, 2020-09-08T12:00:00-01, ",
    )
    fun getNextMoonset(latitude: Double, longitude: Double, time: String, moonset: String?) {
        val actual = Astronomy.getNextMoonset(
            ZonedDateTime.parse(time),
            Coordinate(latitude, longitude)
        )
        if (moonset != null) {
            assertDate(ZonedDateTime.parse(moonset), actual, Duration.ofMinutes(1))
        } else {
            assertNull(actual)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T00:00:00-04, 2020-09-13T01:45:00-04",
        "40.7128, -74.0060, 2020-09-13T02:00:00-04, 2020-09-14T02:51:00-04",
        "76.7667, -18.6667, 2020-09-08T12:00:00-01, ",
    )
    fun getNextMoonrise(latitude: Double, longitude: Double, time: String, moonrise: String?) {
        val actual = Astronomy.getNextMoonrise(
            ZonedDateTime.parse(time),
            Coordinate(latitude, longitude)
        )
        if (moonrise != null) {
            assertDate(ZonedDateTime.parse(moonrise), actual, Duration.ofMinutes(1))
        } else {
            assertNull(actual)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "2020-03-02T14:58:00-05, FirstQuarter, 50.0",
        "2020-03-09T13:48:00-05, Full, 100.0",
        "2020-03-16T05:35:00-04, ThirdQuarter, 50.0",
        "2020-03-24T05:29:00-04, New, 0.0",
        "2020-03-29T12:00:00-04, WaxingCrescent, 23.0",
        "2020-03-05T12:00:00-05, WaxingGibbous, 79.0",
        "2020-03-13T12:00:00-04, WaningGibbous, 79.0",
        "2020-03-18T12:00:00-04, WaningCrescent, 28.0",

        )
    fun getMoonPhase(date: String, phase: MoonTruePhase, illumination: Float) {
        val tolerance = 0.5f

        val actual = Astronomy.getMoonPhase(ZonedDateTime.parse(date))
        assertMoonPhases(
            MoonPhase(phase, illumination),
            actual,
            tolerance
        )
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T12:00:00-04, true",
        "40.7128, -74.0060, 2020-09-13T23:00:00-04, false"
    )
    fun isSunUp(latitude: Double, longitude: Double, time: String, isUp: Boolean) {
        val actual = Astronomy.isSunUp(
            ZonedDateTime.parse(time),
            Coordinate(latitude, longitude)
        )

        assertEquals(isUp, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2020-09-13T09:08:00-04, true",
        "40.7128, -74.0060, 2020-09-13T21:58:00-04, false"
    )
    fun isMoonUp(latitude: Double, longitude: Double, time: String, isUp: Boolean) {
        val actual = Astronomy.isMoonUp(
            ZonedDateTime.parse(time),
            Coordinate(latitude, longitude)
        )

        assertEquals(isUp, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "40.7128, -74.0060, 2023-01-03T00:00:00-05, Quadrantids, 2023-01-03T17:00:00-05",
        "40.7128, -74.0060, 2023-04-22T00:00:00-04, Lyrids, 2023-04-22T05:00:00-04",
        "40.7128, -74.0060, 2023-05-06T00:00:00-04, EtaAquariids, 2023-05-06T04:00:00-04",
        "40.7128, -74.0060, 2023-07-31T00:00:00-04, DeltaAquariids, 2023-07-31T03:00:00-04",
        "40.7128, -74.0060, 2023-08-12T00:00:00-04, Perseids, 2023-08-12T04:00:00-04",
        "40.7128, -74.0060, 2023-10-21T00:00:00-04, Orionids, 2023-10-21T05:00:00-04",
        "40.7128, -74.0060, 2023-11-18T00:00:00-05, Leonids, 2023-11-18T06:00:00-05",
        "40.7128, -74.0060, 2023-12-14T00:00:00-05, Geminids, 2023-12-14T01:00:00-05",
        "40.7128, -74.0060, 2023-12-22T00:00:00-05, Ursids, 2023-12-22T05:00:00-05",
        "40.7128, -74.0060, 2023-01-01T00:00:00-05, , ",
    )
    fun getMeteorShower(
        latitude: Double,
        longitude: Double,
        time: String,
        shower: MeteorShower?,
        peak: String?
    ) {
        val actual = Astronomy.getMeteorShower(
            Coordinate(latitude, longitude),
            ZonedDateTime.parse(time)
        )

        assertEquals(shower, actual?.shower)
        if (peak != null) {
            assertDate(ZonedDateTime.parse(peak), actual?.peak, Duration.ofHours(1))
        } else {
            assertNull(actual)
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
        fun provideTotalLunarEclipses(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    42.0,
                    -70.0,
                    "2021-08-29T00:00:00Z",
                    "2022-05-16T03:29:00Z",
                    "2022-05-16T04:11:00Z",
                    "2022-05-16T04:53:00Z",
                    1.414f,
                    1f
                )
            )
        }

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