package com.kylecorry.sol.time

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.time.Time.atEndOfDay
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.daysUntil
import com.kylecorry.sol.time.Time.duration
import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestPastTime
import com.kylecorry.sol.time.Time.getClosestTime
import com.kylecorry.sol.time.Time.hoursUntil
import com.kylecorry.sol.time.Time.isInPast
import com.kylecorry.sol.time.Time.isOlderThan
import com.kylecorry.sol.time.Time.middle
import com.kylecorry.sol.time.Time.plusHours
import com.kylecorry.sol.time.Time.plusMillis
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
    fun localPlusHours(){
        val time = LocalDateTime.of(2020, 1, 1, 12, 0)
        val expected = LocalDateTime.of(2020, 1, 1, 13, 30)
        val actual = time.plusHours(1.5)
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
    fun duration(){
        val expected = Duration.ofHours(1).plusMinutes(2).plusSeconds(3)
        val actual = duration(1, 2, 3)
        assertEquals(expected, actual)

        assertEquals(Duration.ZERO, Time.duration())
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
    fun isDaylightSavings() {
        val time = zdt(2022, Month.MARCH, 13, 2, zone = ZoneId.of("America/New_York"))
        assertTrue(Time.isDaylightSavings(time))

        val time2 = zdt(2022, Month.NOVEMBER, 6, 2, zone = ZoneId.of("America/New_York"))
        assertFalse(Time.isDaylightSavings(time2))
    }

    @Test
    fun getDaylightSavings() {
        val time = zdt(2022, Month.MARCH, 13, 2, zone = ZoneId.of("America/New_York"))
        assertEquals(Duration.ofHours(1), Time.getDaylightSavings(time))

        val time2 = zdt(2022, Month.NOVEMBER, 6, 2, zone = ZoneId.of("America/New_York"))
        assertEquals(Duration.ofHours(0), Time.getDaylightSavings(time2))
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
    fun canGetClosestPastTimeZoned() {
        val now = zdt(2020, Month.JANUARY, 10, 2)
        val times = listOf(
            zdt(2020, Month.JANUARY, 10, 0),
            zdt(2020, Month.JANUARY, 11, 0),
            zdt(2020, Month.JANUARY, 10, 1),
            null
        )

        val actual = getClosestPastTime(now, times)

        assertEquals(zdt(2020, Month.JANUARY, 10, 1), actual)
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
    fun canGetClosestFutureTimeZoned() {
        val now = zdt(2020, Month.JANUARY, 10, 2)
        val times = listOf(
            zdt(2020, Month.JANUARY, 10, 0),
            zdt(2020, Month.JANUARY, 11, 0),
            zdt(2020, Month.JANUARY, 10, 1),
            null
        )

        val actual = getClosestFutureTime(now, times)

        assertEquals(zdt(2020, Month.JANUARY, 11, 0), actual)
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

    @Test
    fun hoursBetween(){
        val start = zdt(2020, Month.JANUARY, 1, 12)
        val end = zdt(2020, Month.JANUARY, 1, 13)
        val actual = Time.hoursBetween(start, end)
        assertEquals(1f, actual)
    }

    @Test
    fun getReadingsDate(){
        val date = LocalDate.of(2020, Month.JANUARY, 1)
        val step = Duration.ofHours(1)
        val readings = Time.getReadings(date, ZoneId.systemDefault(), step) { it.toEpochSecond() }
        assertEquals(24, readings.size)
        val end = date
            .atTime(23, 0)
            .atZone(ZoneId.systemDefault())
        val start = date
            .atTime(0, 0)
            .atZone(ZoneId.systemDefault())

        assertEquals(start.toEpochSecond(), readings.first().value)
        assertEquals(start.toInstant(), readings.first().time)
        assertEquals(end.toEpochSecond(), readings.last().value)
        assertEquals(end.toInstant(), readings.last().time)
    }

    @Test
    fun getReadingsZoned(){
        val start = zdt(2020, Month.JANUARY, 1, 0)
        val end = zdt(2020, Month.JANUARY, 1, 23)
        val step = Duration.ofHours(1)
        val readings = Time.getReadings(start, end, step) { it.toEpochSecond() }
        assertEquals(24, readings.size)
        val startInstant = start.toInstant()
        val endInstant = end.toInstant()
        assertEquals(startInstant, readings.first().time)
        assertEquals(start.toEpochSecond(), readings.first().value)
        assertEquals(endInstant, readings.last().time)
        assertEquals(end.toEpochSecond(), readings.last().value)
    }

    @Test
    fun getReadingsBackwards(){
        val start = zdt(2020, Month.JANUARY, 1, 0)
        val end = zdt(2019, Month.DECEMBER, 30, 0)
        val step = Duration.ofHours(1)
        val readings = Time.getReadings(start, end, step) { it.toEpochSecond() }
        assertEquals(0, readings.size)
    }

    @Test
    fun getReadingsNoStep(){
        val start = zdt(2020, Month.JANUARY, 1, 0)
        val end = zdt(2020, Month.JANUARY, 1, 23)
        val step = Duration.ZERO
        val readings = Time.getReadings(start, end, step) { it.toEpochSecond() }
        assertEquals(0, readings.size)
    }

    @Test
    fun getReadingsNegativeStep(){
        val start = zdt(2020, Month.JANUARY, 1, 0)
        val end = zdt(2020, Month.JANUARY, 1, 23)
        val step = Duration.ofHours(-1)
        val readings = Time.getReadings(start, end, step) { it.toEpochSecond() }
        assertEquals(0, readings.size)
    }

    @Test
    fun getYearlyValues(){
        val year = 2020
        val values = Time.getYearlyValues(year) { it.dayOfYear.toLong() }
        assertEquals(366, values.size)
        assertEquals(LocalDate.of(year, Month.JANUARY, 1) to 1L, values.first())
        assertEquals(LocalDate.of(year, Month.DECEMBER, 31) to 366L, values.last())
    }

    @Test
    fun durationOfReadings(){
        val start = zdt(2020, Month.JANUARY, 1, 0)
        val end = zdt(2020, Month.JANUARY, 1, 23)
        val step = Duration.ofHours(1)
        val readings = Time.getReadings(start, end, step) { it.toEpochSecond() }
        val duration = readings.duration()
        assertEquals(Duration.ofHours(23), duration)
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

    @Test
    fun roundNearestMinuteLocalDefault(){
        val time = LocalDateTime.of(2020, Month.JANUARY, 1, 1, 23, 45)
        val actual = time.roundNearestMinute()
        val expected = LocalDateTime.of(2020, Month.JANUARY, 1, 1, 24)
        assertEquals(expected, actual)
    }

    @Test
    fun roundNearestMinuteZonedDefault(){
        val time = zdt(2020, Month.JANUARY, 1, 1, 23).withSecond(45)
        val actual = time.roundNearestMinute()
        val expected = zdt(2020, Month.JANUARY, 1, 1, 24)
        assertEquals(expected, actual)
    }

    @Test
    fun middle(){
        val start = zdt(2020, Month.JANUARY, 1, 0)
        val end = zdt(2020, Month.JANUARY, 2, 0)
        val actual = Range(start, end).middle()
        val expected = zdt(2020, Month.JANUARY, 1, 12)
        assertEquals(expected, actual)
    }

    @Test
    fun plusMillisLocal(){
        val time = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0)
        val actual = time.plusMillis(1000)
        val expected = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 1, 0)
        assertEquals(expected, actual)
    }

    @Test
    fun plusMillisZoned(){
        val time = zdt(2020, Month.JANUARY, 1, 0, 0)
        val actual = time.plusMillis(1000)
        val expected = zdt(2020, Month.JANUARY, 1, 0, 0).withSecond(1)
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