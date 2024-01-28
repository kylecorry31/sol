package com.kylecorry.sol.time

import com.kylecorry.sol.time.Time.getClosestFutureTime
import com.kylecorry.sol.time.Time.getClosestPastTime
import com.kylecorry.sol.time.Time.getClosestTime
import com.kylecorry.sol.time.Time.roundNearestMinute
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.*

class TimeTest {

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