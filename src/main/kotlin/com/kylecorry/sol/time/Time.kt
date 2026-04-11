package com.kylecorry.sol.time

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Reading
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object Time {
    fun hours(hours: Double): Duration {
        val minutes = hours * 60
        val seconds = minutes * 60
        val millis = seconds * 1000
        return Duration.ofMillis(millis.toLong())
    }

    fun hours(duration: Duration): Double = duration.toMillis() / 1000.0 / 60.0 / 60.0

    fun LocalDateTime.toZonedDateTime(): ZonedDateTime = ZonedDateTime.of(this, ZoneId.systemDefault())

    fun LocalDateTime.toUTC(): ZonedDateTime = ZonedDateTime.of(this, ZoneId.of("UTC"))

    fun LocalDateTime.toEpochMillis(): Long = this.toZonedDateTime().toInstant().toEpochMilli()

    fun ZonedDateTime.atStartOfDay(): ZonedDateTime = ZonedDateTime.of(this.toLocalDate(), LocalTime.MIN, this.zone)

    fun ZonedDateTime.atEndOfDay(): ZonedDateTime = ZonedDateTime.of(this.toLocalDate(), LocalTime.MAX, this.zone)

    fun LocalDate.atEndOfDay(): LocalDateTime = atTime(LocalTime.MAX)

    fun LocalDateTime.plusHours(hours: Double): LocalDateTime = this.plus(hours(hours))

    fun ZonedDateTime.toUTCLocal(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))

    fun Instant.utc(): ZonedDateTime = ZonedDateTime.ofInstant(this, ZoneId.of("UTC"))

    fun Instant.toZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(this, ZoneId.systemDefault())

    fun Instant.isInPast(): Boolean = this < Instant.now()

    fun Instant.isOlderThan(duration: Duration): Boolean = Duration.between(this, Instant.now()) > duration

    fun duration(
        hours: Long = 0L,
        minutes: Long = 0L,
        seconds: Long = 0L,
    ): Duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)

    fun Instant.hoursUntil(other: Instant): Float = Duration.between(this, other).seconds / (60f * 60f)

    fun LocalDate.daysUntil(other: LocalDate): Long = Duration.between(this.atStartOfDay(), other.atStartOfDay()).toDays()

    fun Instant.plusHours(hours: Long): Instant = plus(Duration.ofHours(hours))

    fun getClosestPastTime(
        currentTime: Instant,
        times: List<Instant?>,
    ): Instant? =
        times
            .filterNotNull()
            .filter { it.isBefore(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }

    fun getClosestPastTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>,
    ): LocalDateTime? =
        times
            .filterNotNull()
            .filter { it.isBefore(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }

    fun getClosestFutureTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>,
    ): LocalDateTime? =
        times
            .filterNotNull()
            .filter { it.isAfter(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }

    fun getClosestTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>,
    ): ZonedDateTime? = times.filterNotNull().minByOrNull { Duration.between(it, currentTime).abs() }

    fun getClosestFutureTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>,
    ): ZonedDateTime? =
        times
            .filterNotNull()
            .filter { it.isAfter(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }

    fun getClosestPastTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>,
    ): ZonedDateTime? =
        times
            .filterNotNull()
            .filter { it.isBefore(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }

    fun hoursBetween(
        first: Temporal,
        second: Temporal,
    ): Float = Duration.between(first, second).seconds / 3600f

    inline fun <T> getReadings(
        date: LocalDate,
        zone: ZoneId,
        step: Duration,
        valueFn: (time: ZonedDateTime) -> T,
    ): List<Reading<T>> =
        getReadings(
            date.atStartOfDay().atZone(zone),
            date.atEndOfDay().atZone(zone),
            step,
            valueFn,
        )

    inline fun <T> getReadings(
        start: ZonedDateTime,
        end: ZonedDateTime,
        step: Duration,
        valueFn: (time: ZonedDateTime) -> T,
    ): List<Reading<T>> {
        if (step.isZero || step.isNegative) {
            return emptyList()
        }

        val readings = mutableListOf<Reading<T>>()
        var time = start
        while (time <= end) {
            readings.add(Reading(valueFn(time), time.toInstant()))
            time = time.plus(step)
        }
        return readings
    }

    inline fun <T> getYearlyValues(
        year: Int,
        valueProvider: (date: LocalDate) -> T,
    ): List<Pair<LocalDate, T>> {
        val values = mutableListOf<Pair<LocalDate, T>>()
        var date = LocalDate.of(year, Month.JANUARY, 1)

        while (date.year == year) {
            values.add(date to valueProvider(date))
            date = date.plusDays(1)
        }

        return values
    }

    fun List<Reading<*>>.duration(): Duration {
        val start = minByOrNull { it.time } ?: return Duration.ZERO
        val end = maxByOrNull { it.time } ?: return Duration.ZERO
        return Duration.between(start.time, end.time)
    }

    fun isDaylightSavings(time: ZonedDateTime): Boolean = time.zone.rules.isDaylightSavings(time.toInstant())

    fun getDaylightSavings(time: ZonedDateTime): Duration = time.zone.rules.getDaylightSavings(time.toInstant())

    fun getDaylightSavingsTransitions(
        zone: ZoneId,
        year: Int,
    ): List<Pair<ZonedDateTime, Duration>> {
        val dates = mutableListOf<Pair<ZonedDateTime, Duration>>()
        var date = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zone)
        val maxIterations = 100
        var iterations = 0
        while (date.year == year && iterations < maxIterations) {
            val next = zone.rules.nextTransition(date.toInstant()) ?: break
            date = ZonedDateTime.ofInstant(next.instant, zone)
            val savings = getDaylightSavings(date)
            if (date.year == year && dates.lastOrNull()?.second != savings) {
                dates.add(date to getDaylightSavings(date))
            }
            iterations++
        }
        return dates
    }

    fun <T> Range<T>.middle(): T where T : Temporal, T : Comparable<T> {
        @Suppress("UNCHECKED_CAST")
        return start.plus(Duration.between(start, end).dividedBy(2)) as T
    }

    fun ZonedDateTime.roundNearestMinute(minutes: Int = 1): ZonedDateTime {
        val seconds = this.second
        if (minutes == 1) {
            return if (seconds >= 30) {
                this.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)
            } else {
                this.truncatedTo(ChronoUnit.MINUTES)
            }
        }

        val nanosPerMinute = 60_000_000_000L
        val intervalNanos = minutes * nanosPerMinute
        val totalNanos = this.minute * nanosPerMinute + this.second * 1_000_000_000L + this.nano
        val roundedNanos = ((totalNanos + intervalNanos / 2) / intervalNanos) * intervalNanos
        return this.plusNanos(roundedNanos - totalNanos).truncatedTo(ChronoUnit.MINUTES)
    }

    fun LocalDateTime.roundNearestMinute(minutes: Int = 1): LocalDateTime {
        val seconds = this.second
        if (minutes == 1) {
            return if (seconds >= 30) {
                this.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)
            } else {
                this.truncatedTo(ChronoUnit.MINUTES)
            }
        }

        val nanosPerMinute = 60_000_000_000L
        val intervalNanos = minutes * nanosPerMinute
        val totalNanos = this.minute * nanosPerMinute + this.second * 1_000_000_000L + this.nano
        val roundedNanos = ((totalNanos + intervalNanos / 2) / intervalNanos) * intervalNanos
        return this.plusNanos(roundedNanos - totalNanos).truncatedTo(ChronoUnit.MINUTES)
    }

    fun LocalDateTime.plusMillis(millis: Long): LocalDateTime = this.plusNanos(millis * 1000000L)

    fun ZonedDateTime.plusMillis(millis: Long): ZonedDateTime = this.plusNanos(millis * 1000000L)

    fun getSolarTimeOffset(longitude: Double): Duration = hours(longitude / 15)

    fun getLongitudeFromSolarTimeOffset(offset: Duration): Double = hours(offset) * 15

    fun getApproximateTimeZone(location: Coordinate): ZoneId {
        val offset = getSolarTimeOffset(location.longitude)
        val offsetHours = hours(offset).roundToInt()
        val symbol = if (offsetHours >= 0) "+" else "-"
        return ZoneId.of("${symbol}${offsetHours.absoluteValue}")
    }

    fun getLocationFromTimeZone(zone: ZoneId): Coordinate {
        val lookupLocation = TimeZoneLocations.getLocation(zone.id)
        if (lookupLocation != null) {
            return lookupLocation
        }

        val offset =
            Duration.ofSeconds(
                zone.rules
                    .getStandardOffset(Instant.now())
                    .totalSeconds
                    .toLong(),
            )
        val timezoneLongitude = getLongitudeFromSolarTimeOffset(offset)
        return Coordinate(0.0, timezoneLongitude)
    }
}
