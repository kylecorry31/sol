package com.kylecorry.sol.time

import com.kylecorry.sol.time.Time.atEndOfDay
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.daysUntil
import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestPastTime
import com.kylecorry.sol.time.Time.getClosestTime
import com.kylecorry.sol.time.Time.hoursUntil
import com.kylecorry.sol.time.Time.isInPast
import com.kylecorry.sol.time.Time.isOlderThan
import com.kylecorry.sol.time.Time.plusHours
import com.kylecorry.sol.time.Time.roundNearestMinute
import com.kylecorry.sol.time.Time.toEpochMillis
import com.kylecorry.sol.time.Time.toUTC
import com.kylecorry.sol.time.Time.toUTCLocal
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.time.Time.utc
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.*

class TimeTest {

    @ParameterizedTest
    @CsvSource(
        "100, PT100H",
        "24, PT24H",
        "1.5, PT1H30M",
        "1, PT1H",
        "0.5, PT30M",
        "0.03125, PT1M52S",
        "0.00048828125, PT1S",
    )
    fun hours(hours: Double, expectedStr: String){
        val expected = Duration.parse(expectedStr)
        val actual = Time.hours(hours)
        assertDurationEquals(expected, actual, Duration.ofSeconds(1))
    }

    @Test
    fun toZonedDateTime() {
        val time = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0)
        val expected = ZonedDateTime.of(time, ZoneId.systemDefault())
        val actual = time.toZonedDateTime()
        assertEquals(expected, actual)
    }

    @Test
    fun localToUTC() {
        val time = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0)
        val expected = ZonedDateTime.of(time, ZoneId.of("UTC"))
        val actual = time.toUTC()
        assertEquals(expected, actual)
    }

    @Test
    fun localToEpochMillis() {
        val time = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0)
        val expected = ZonedDateTime.of(time, ZoneId.systemDefault()).toEpochSecond() * 1000
        val actual = time.toEpochMillis()
        assertEquals(expected, actual)
    }

    @Test
    fun zonedAtStartOfDay() {
        val time = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.systemDefault())
        val expected = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())
        val actual = time.atStartOfDay()
        assertEquals(expected, actual)
    }

    @Test
    fun zonedAtEndOfDay() {
        val time = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.systemDefault())
        val expected = ZonedDateTime.of(2020, 1, 1, 23, 59, 59, 999999999, ZoneId.systemDefault())
        val actual = time.atEndOfDay()
        assertEquals(expected, actual)
    }

    @Test
    fun localAtStartOfDay() {
        val time = LocalDate.of(2020, 1, 1)
        val expected = LocalDateTime.of(2020, 1, 1, 0, 0)
        val actual = time.atStartOfDay()
        assertEquals(expected, actual)
    }

    @Test
    fun localAtEndOfDay() {
        val time = LocalDate.of(2020, 1, 1)
        val expected = LocalDateTime.of(2020, 1, 1, 23, 59, 59, 999999999)
        val actual = time.atEndOfDay()
        assertEquals(expected, actual)
    }

    @Test
    fun toUtcLocal(){
        val time = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.of("America/New_York"))
        val expected = LocalDateTime.of(2020, 1, 1, 17, 0, 0, 0)
        val actual = time.toUTCLocal()
        assertEquals(expected, actual)
    }

    @Test
    fun instantToUtcZoned(){
        val time = Instant.ofEpochMilli(0)
        val expected = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        val actual = time.utc()
        assertEquals(expected, actual)
    }

    @Test
    fun instantToZoned(){
        val time = Instant.ofEpochMilli(24 * 60 * 60 * 1000)
        val expected = ZonedDateTime.of(1970, 1, 2, 0, 0, 0, 0, ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault())
        val actual = time.toZonedDateTime()
        assertEquals(expected, actual)
    }

    @Test
    fun instantIsInPast(){
        val past = Instant.now().minusSeconds(1)
        val future = Instant.now().plusSeconds(1)
        assertTrue(past.isInPast())
        assertFalse(future.isInPast())
    }

    @Test
    fun instantIsOlderThan(){
        val time = Instant.now().minusSeconds(100)
        assertTrue(time.isOlderThan(Duration.ofSeconds(10)))
        assertFalse(time.isOlderThan(Duration.ofSeconds(110)))
    }

    @Test
    fun hoursUntil(){
        val start = Instant.now()
        val end = start.plusSeconds(3600)
        val actual = start.hoursUntil(end)
        assertEquals(1f, actual)
    }

    @Test
    fun daysBetweenLocalDates(){
        val start = LocalDate.of(2020, 1, 1)
        val end = LocalDate.of(2020, 1, 2)
        val actual = start.daysUntil(end)
        assertEquals(1, actual)
    }

    @Test
    fun instantPlusHours(){
        val time = Instant.now()
        val expected = time.plus(Duration.ofHours(1))
        val actual = time.plusHours(1)
        assertEquals(expected, actual)
    }

    @Test
    fun getDaylightSavingsTransitions() {
        val zone = ZoneId.of("America/New_York")
        val year = 2022

        val times = Time.getDaylightSavingsTransitions(zone, year)
        assertEquals(
            times, listOf(
                zdt(2022, Month.MARCH, 13, 2, zone = zone) to Duration.ofHours(1),
                zdt(2022, Month.NOVEMBER, 6, 1, zone = zone).plusHours(1) to Duration.ofHours(0),
            )
        )
    }

    @Test
    fun getDaylightSavingsTransitionsNotObserved() {
        val zone = ZoneId.of("Asia/Shanghai")
        val year = 2022

        val times = Time.getDaylightSavingsTransitions(zone, year)
        assertEquals(
            times, listOf<Pair<ZonedDateTime, Duration>>()
        )
    }

    @Test
    fun canGetClosestPastTime() {
        val now = dt(2020, Month.JANUARY, 10, 2)
        val times = listOf(
            dt(2020, Month.JANUARY, 10, 0),
            dt(2020, Month.JANUARY, 11, 0),
            dt(2020, Month.JANUARY, 10, 1),
            null
        )

        val actual = getClosestPastTime(now, times)

        assertEquals(dt(2020, Month.JANUARY, 10, 1), actual)
    }

    @Test
    fun canGetClosestPastTimeInstant(){
        val now = Instant.now()
        val times = listOf(
            now.minusSeconds(100),
            now.minusSeconds(10),
            now.plusSeconds(100),
            null
        )

        val actual = getClosestPastTime(now, times)

        assertEquals(now.minusSeconds(10), actual)
    }

    @Test
    fun canGetClosestTime() {
        val now = zdt(2020, Month.JANUARY, 10, 2)
        val times = listOf(
            zdt(2020, Month.JANUARY, 10, 0),
            zdt(2020, Month.JANUARY, 11, 0),
            zdt(2020, Month.JANUARY, 10, 1),
            zdt(2020, Month.JANUARY, 10, 2, 30),
            null
        )

        val actual = getClosestTime(now, times)

        assertEquals(zdt(2020, Month.JANUARY, 10, 2, 30), actual)
    }


    @Test
    fun canGetClosestFutureTime() {
        val now = dt(2020, Month.JANUARY, 10, 2)
        val times = listOf(
            dt(2020, Month.JANUARY, 10, 0),
            dt(2020, Month.JANUARY, 11, 0),
            dt(2020, Month.JANUARY, 10, 1),
            null
        )

        val actual = getClosestFutureTime(now, times)

        assertEquals(dt(2020, Month.JANUARY, 11, 0), actual)
    }

    @Test
    fun returnsNullIfNoFutureTimes() {
        val now = dt(2020, Month.JANUARY, 10, 2)
        val times = listOf(
            dt(2020, Month.JANUARY, 10, 0),
            dt(2020, Month.JANUARY, 10, 1),
            null
        )

        val actual = getClosestFutureTime(now, times)

        assertNull(actual)
    }

    @Test
    fun returnsNullIfNoPastTimes() {
        val now = dt(2020, Month.JANUARY, 9, 2)
        val times = listOf(
            dt(2020, Month.JANUARY, 10, 0),
            dt(2020, Month.JANUARY, 10, 1),
            null
        )

        val actual = getClosestPastTime(now, times)

        assertNull(actual)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01-01T01:23:45-05:00, 1, 2020-01-01T01:24:00-05:00",
        "2020-01-01T01:23:24-05:00, 1, 2020-01-01T01:23:00-05:00",
        "2020-01-01T01:23:45-05:00, 5, 2020-01-01T01:25:00-05:00",
        "2020-01-01T01:23:45-05:00, 15, 2020-01-01T01:30:00-05:00",
        "2020-01-01T01:19:45-05:00, 15, 2020-01-01T01:15:00-05:00",
        "2020-01-01T01:59:45-05:00, 15, 2020-01-01T02:00:00-05:00",
        "2020-01-01T23:59:45-05:00, 15, 2020-01-02T00:00:00-05:00",
        "2020-01-01T01:30:00-05:00, 15, 2020-01-01T01:30:00-05:00",
    )
    fun roundNearestMinute(time: ZonedDateTime, minute: Int, expected: ZonedDateTime) {
        val actual = time.roundNearestMinute(minute)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01-01T01:23:45, 1, 2020-01-01T01:24:00",
        "2020-01-01T01:23:24, 1, 2020-01-01T01:23:00",
        "2020-01-01T01:23:45, 5, 2020-01-01T01:25:00",
        "2020-01-01T01:23:45, 15, 2020-01-01T01:30:00",
        "2020-01-01T01:19:45, 15, 2020-01-01T01:15:00",
        "2020-01-01T01:59:45, 15, 2020-01-01T02:00:00",
        "2020-01-01T23:59:45, 15, 2020-01-02T00:00:00",
        "2020-01-01T01:30:00, 15, 2020-01-01T01:30:00",
    )
    fun roundNearestMinute(time: LocalDateTime, minute: Int, expected: LocalDateTime) {
        val actual = time.roundNearestMinute(minute)
        assertEquals(expected, actual)
    }

    private fun assertDurationEquals(expected: Duration, actual: Duration, delta: Duration) {
        val diff = expected.minus(actual).abs()
        if (diff > delta) {
            assertEquals(expected, actual)
        }
    }

    private fun dt(year: Int, month: Month, day: Int, hour: Int, minute: Int = 0): LocalDateTime {
        return LocalDateTime.of(year, month, day, hour, minute)
    }

    private fun zdt(
        year: Int,
        month: Month,
        day: Int,
        hour: Int,
        minute: Int = 0,
        zone: ZoneId = ZoneId.systemDefault()
    ): ZonedDateTime {
        return ZonedDateTime.of(dt(year, month, day, hour, minute), zone)
    }
}