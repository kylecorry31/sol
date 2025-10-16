package com.kylecorry.sol.time
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.roundNearest
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Reading
import kotlinx.datetime.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.time.Duration

object Time {

    fun hours(hours: Double): Duration {
        return Duration.parse("PT${hours}H")
    }

    fun hours(duration: Duration): Double {
        return duration.inWholeMilliseconds / 1000.0 / 60.0 / 60.0
    }

    fun LocalDateTime.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime(this, TimeZone.currentSystemDefault())
    }

    fun LocalDateTime.toUTC(): ZonedDateTime {
        return ZonedDateTime(this, TimeZone.UTC)
    }

    fun LocalDateTime.toEpochMillis(): Long {
        return this.toZonedDateTime().toEpochSecond() * 1000
    }

    fun ZonedDateTime.atStartOfDay(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime(0, 0), this.zone)
    }

    fun ZonedDateTime.atEndOfDay(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime(23, 59, 59, 999_999_999), this.zone)
    }

    fun LocalDate.atEndOfDay(): LocalDateTime {
        return LocalDateTime(this, LocalTime(23, 59, 59, 999_999_999))
    }

    fun LocalDateTime.plusHours(hours: Double): LocalDateTime {
        val duration = hours(hours)
        return this.toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
    }

    fun ZonedDateTime.toUTCLocal(): LocalDateTime {
        return instant.toLocalDateTime(TimeZone.UTC)
    }

    fun Instant.utc(): ZonedDateTime {
        return ZonedDateTime.ofInstant(this, TimeZone.UTC)
    }

    fun Instant.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.ofInstant(this, TimeZone.currentSystemDefault())
    }

    fun Instant.isInPast(): Boolean {
        return this < Clock.System.now()
    }

    fun Instant.isOlderThan(duration: Duration): Boolean {
        return (Clock.System.now() - this) > duration
    }

    fun duration(hours: Long = 0L, minutes: Long = 0L, seconds: Long = 0L): Duration {
        return Duration.parse("PT${hours}H${minutes}M${seconds}S")
    }

    fun Instant.hoursUntil(other: Instant): Float {
        return (other - this).inWholeSeconds / (60f * 60f)
    }

    fun LocalDate.daysUntil(other: LocalDate): Long {
        return (this.daysUntil(other)).toLong()
    }

    fun Instant.plusHours(hours: Long): Instant {
        return plus(Duration.parse("PT${hours}H"))
    }

    fun getClosestPastTime(
        currentTime: Instant,
        times: List<Instant?>
    ): Instant? {
        return times.filterNotNull().filter { it < currentTime }
            .minByOrNull { (it - currentTime).absoluteValue }
    }

    fun getClosestPastTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>
    ): LocalDateTime? {
        return times.filterNotNull().filter { it < currentTime }
            .minByOrNull { (it.toInstant(TimeZone.UTC) - currentTime.toInstant(TimeZone.UTC)).absoluteValue }
    }

    fun getClosestFutureTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>
    ): LocalDateTime? {
        return times.filterNotNull().filter { it > currentTime }
            .minByOrNull { (it.toInstant(TimeZone.UTC) - currentTime.toInstant(TimeZone.UTC)).absoluteValue }
    }

    fun getClosestTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().minByOrNull { (it.instant - currentTime.instant).absoluteValue }
    }

    fun getClosestFutureTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().filter { it.isAfter(currentTime) }
            .minByOrNull { (it.instant - currentTime.instant).absoluteValue }
    }

    fun getClosestPastTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().filter { it.isBefore(currentTime) }
            .minByOrNull { (it.instant - currentTime.instant).absoluteValue }
    }

    fun hoursBetween(first: Instant, second: Instant): Float {
        return (second - first).inWholeSeconds / 3600f
    }

    inline fun <T> getReadings(
        date: LocalDate,
        zone: TimeZone,
        step: Duration,
        valueFn: (time: ZonedDateTime) -> T
    ): List<Reading<T>> {
        val startOfDay = LocalDateTime(date, LocalTime(0, 0))
        val endOfDay = LocalDateTime(date, LocalTime(23, 59, 59, 999_999_999))
        return getReadings(
            ZonedDateTime(startOfDay, zone),
            ZonedDateTime(endOfDay, zone),
            step,
            valueFn
        )
    }

    inline fun <T> getReadings(
        start: ZonedDateTime,
        end: ZonedDateTime,
        step: Duration,
        valueFn: (time: ZonedDateTime) -> T
    ): List<Reading<T>> {

        if (step.isNegative() || step == Duration.ZERO) {
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
        valueProvider: (date: LocalDate) -> T
    ): List<Pair<LocalDate, T>> {
        val values = mutableListOf<Pair<LocalDate, T>>()
        var date = LocalDate(year, Month.JANUARY, 1)

        while (date.year == year) {
            values.add(date to valueProvider(date))
            date = date.plus(1, DateTimeUnit.DAY)
        }

        return values
    }

    fun List<Reading<*>>.duration(): Duration {
        val start = minByOrNull { it.time } ?: return Duration.ZERO
        val end = maxByOrNull { it.time } ?: return Duration.ZERO
        return (end.time - start.time)
    }

    fun isDaylightSavings(time: ZonedDateTime): Boolean {
        val offset = time.zone.offsetAt(time.instant)
        val standardOffset = time.zone.offsetAt(Instant.fromEpochSeconds(0))
        return offset != standardOffset
    }

    fun getDaylightSavings(time: ZonedDateTime): Duration {
        val offset = time.zone.offsetAt(time.instant)
        val standardOffset = time.zone.offsetAt(Instant.fromEpochSeconds(0))
        return Duration.parse("PT${(offset.totalSeconds - standardOffset.totalSeconds)}S")
    }

    fun getDaylightSavingsTransitions(
        zone: TimeZone,
        year: Int
    ): List<Pair<ZonedDateTime, Duration>> {
        // Note: kotlinx-datetime doesn't provide transition information
        // This is a simplified implementation that may not work correctly
        // for all timezones
        return emptyList()
    }

    fun <T> Range<T>.middle(): T where T : ZonedDateTime {
        val start = this.start as ZonedDateTime
        val end = this.end as ZonedDateTime
        val duration = end.instant - start.instant
        @Suppress("UNCHECKED_CAST")
        return ZonedDateTime(start.instant + duration / 2, start.zone) as T
    }

    fun ZonedDateTime.roundNearestMinute(minutes: Int = 1): ZonedDateTime {
        val seconds = this.second
        if (minutes == 1) {
            return if (seconds >= 30) {
                this.plusMinutes(1).truncatedTo(DateTimeUnit.MINUTE)
            } else {
                this.truncatedTo(DateTimeUnit.MINUTE)
            }
        }

        val roundedMinutes = this.minute.roundNearest(minutes)

        val delta = roundedMinutes - this.minute

        return this.plusMinutes(delta.toLong()).truncatedTo(DateTimeUnit.MINUTE)
    }

    fun LocalDateTime.roundNearestMinute(minutes: Int = 1): LocalDateTime {
        val seconds = this.second
        if (minutes == 1) {
            return if (seconds >= 30) {
                LocalDateTime(year, monthNumber, dayOfMonth, hour, minute + 1, 0, 0)
            } else {
                LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, 0, 0)
            }
        }

        val roundedMinutes = this.minute.roundNearest(minutes)

        val delta = roundedMinutes - this.minute

        val newMinute = (minute + delta).coerceIn(0, 59)
        return LocalDateTime(year, monthNumber, dayOfMonth, hour, newMinute, 0, 0)
    }

    fun LocalDateTime.plusMillis(millis: Long): LocalDateTime {
        return this.toInstant(TimeZone.UTC).plus(Duration.parse("PT${millis / 1000.0}S"))
            .toLocalDateTime(TimeZone.UTC)
    }

    fun ZonedDateTime.plusMillis(millis: Long): ZonedDateTime {
        return this.plusNanos(millis * 1000000L)
    }

    fun getSolarTimeOffset(longitude: Double): Duration {
        return hours(longitude / 15)
    }

    fun getLongitudeFromSolarTimeOffset(offset: Duration): Double {
        return hours(offset) * 15
    }

    fun getApproximateTimeZone(location: Coordinate): TimeZone {
        val offset = getSolarTimeOffset(location.longitude)
        val offsetHours = hours(offset).roundToInt()
        val symbol = if (offsetHours >= 0) "+" else "-"
        return TimeZone.of("${symbol}${offsetHours.absoluteValue.toString().padStart(2, '0')}:00")
    }

    fun getLocationFromTimeZone(zone: TimeZone): Coordinate {
        val lookupLocation = TimeZoneLocations.getLocation(zone.id)
        if (lookupLocation != null) {
            return lookupLocation
        }

        val offset = zone.offsetAt(Clock.System.now())
        val offsetDuration = Duration.parse("PT${offset.totalSeconds}S")
        val timezoneLongitude = getLongitudeFromSolarTimeOffset(offsetDuration)
        return Coordinate(0.0, timezoneLongitude)
    }

}