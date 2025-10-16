package com.kylecorry.sol.time

import kotlinx.datetime.*
import kotlin.time.Duration as KDuration

/**
 * A timezone-aware datetime representation using kotlinx-datetime
 */
data class ZonedDateTime(
    val instant: Instant,
    val zone: TimeZone
) : Comparable<ZonedDateTime> {

    constructor(localDateTime: LocalDateTime, zone: TimeZone) : this(
        localDateTime.toInstant(zone),
        zone
    )

    val year: Int get() = toLocalDateTime().year
    val monthNumber: Int get() = toLocalDateTime().monthNumber
    val month: Month get() = toLocalDateTime().month
    val dayOfMonth: Int get() = toLocalDateTime().dayOfMonth
    val dayOfWeek: DayOfWeek get() = toLocalDateTime().dayOfWeek
    val dayOfYear: Int get() = toLocalDateTime().dayOfYear
    val hour: Int get() = toLocalDateTime().hour
    val minute: Int get() = toLocalDateTime().minute
    val second: Int get() = toLocalDateTime().second
    val nanosecond: Int get() = toLocalDateTime().nanosecond

    fun toLocalDateTime(): LocalDateTime = instant.toLocalDateTime(zone)
    fun toLocalDate(): LocalDate = toLocalDateTime().date
    fun toLocalTime(): LocalTime = toLocalDateTime().time
    fun toInstant(): Instant = instant
    fun toEpochSecond(): Long = instant.epochSeconds

    fun plusDays(days: Long): ZonedDateTime = 
        copy(instant = instant.plus(days.toInt(), DateTimeUnit.DAY, zone))
    
    fun minusDays(days: Long): ZonedDateTime =
        copy(instant = instant.minus(days.toInt(), DateTimeUnit.DAY, zone))
    
    fun plusHours(hours: Long): ZonedDateTime =
        copy(instant = instant.plus(hours.toInt(), DateTimeUnit.HOUR, zone))
    
    fun plusMinutes(minutes: Long): ZonedDateTime =
        copy(instant = instant.plus(minutes.toInt(), DateTimeUnit.MINUTE, zone))
    
    fun plusSeconds(seconds: Long): ZonedDateTime =
        copy(instant = instant.plus(seconds.toInt(), DateTimeUnit.SECOND, zone))
    
    fun plusNanos(nanos: Long): ZonedDateTime =
        copy(instant = instant.plus(KDuration.parse("PT${nanos / 1_000_000_000.0}S")))
    
    fun plus(duration: KDuration): ZonedDateTime =
        copy(instant = instant.plus(duration))
    
    fun truncatedTo(unit: DateTimeUnit): ZonedDateTime {
        val ldt = toLocalDateTime()
        val truncated = when (unit) {
            DateTimeUnit.DAY -> LocalDateTime(ldt.year, ldt.monthNumber, ldt.dayOfMonth, 0, 0)
            DateTimeUnit.HOUR -> LocalDateTime(ldt.year, ldt.monthNumber, ldt.dayOfMonth, ldt.hour, 0)
            DateTimeUnit.MINUTE -> LocalDateTime(ldt.year, ldt.monthNumber, ldt.dayOfMonth, ldt.hour, ldt.minute)
            DateTimeUnit.SECOND -> LocalDateTime(ldt.year, ldt.monthNumber, ldt.dayOfMonth, ldt.hour, ldt.minute, ldt.second)
            else -> ldt
        }
        return ZonedDateTime(truncated, zone)
    }

    fun isBefore(other: ZonedDateTime): Boolean = instant < other.instant
    fun isAfter(other: ZonedDateTime): Boolean = instant > other.instant

    override fun compareTo(other: ZonedDateTime): Int = instant.compareTo(other.instant)

    override fun toString(): String = "${toLocalDateTime()}[$zone]"

    companion object {
        fun now(): ZonedDateTime = ZonedDateTime(Clock.System.now(), TimeZone.currentSystemDefault())
        
        fun now(zone: TimeZone): ZonedDateTime = ZonedDateTime(Clock.System.now(), zone)
        
        fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int, nanoOfSecond: Int, zone: TimeZone): ZonedDateTime {
            val localDateTime = LocalDateTime(year, month, dayOfMonth, hour, minute, second, nanoOfSecond)
            return ZonedDateTime(localDateTime, zone)
        }

        fun of(localDateTime: LocalDateTime, zone: TimeZone): ZonedDateTime =
            ZonedDateTime(localDateTime, zone)
        
        fun of(localDate: LocalDate, localTime: LocalTime, zone: TimeZone): ZonedDateTime =
            ZonedDateTime(LocalDateTime(localDate, localTime), zone)
        
        fun ofInstant(instant: Instant, zone: TimeZone): ZonedDateTime =
            ZonedDateTime(instant, zone)
    }
}
