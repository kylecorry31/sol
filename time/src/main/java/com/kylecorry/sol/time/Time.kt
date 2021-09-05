package com.kylecorry.sol.time

import java.time.*

object Time {

    fun hours(hours: Double): Duration {
        val minutes = hours * 60
        val seconds = minutes * 60
        val millis = seconds * 1000
        return Duration.ofMillis(millis.toLong())
    }

    fun LocalDateTime.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.of(this, ZoneId.systemDefault())
    }

    fun LocalDateTime.toEpochMillis(): Long {
        return this.toZonedDateTime().toEpochSecond() * 1000
    }

    fun ZonedDateTime.atStartOfDay(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime.MIN, this.zone)
    }

    fun ZonedDateTime.atEndOfDay(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime.MAX, this.zone)
    }

    fun LocalDateTime.roundNearestMinute(minutes: Long): LocalDateTime {
        val minute = this.minute
        val newMinute = (minute / minutes) * minutes

        val diff = newMinute - minute
        return this.plusMinutes(diff)
    }

    fun LocalDateTime.plusHours(hours: Double): LocalDateTime {
        return this.plus(hours(hours))
    }

    fun ZonedDateTime.toUTCLocal(): LocalDateTime {
        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
    }

    fun Instant.utc(): ZonedDateTime {
        return ZonedDateTime.ofInstant(this, ZoneId.of("UTC"))
    }

    fun Instant.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
    }

    fun Instant.isInPast(): Boolean {
        return this < Instant.now()
    }

    fun Instant.isOlderThan(duration: Duration): Boolean {
        return Duration.between(this, Instant.now()) > duration
    }

    fun duration(hours: Long = 0L, minutes: Long = 0L, seconds: Long = 0L): Duration {
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
    }

    fun getClosestPastTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>
    ): LocalDateTime? {
        return times.filterNotNull().filter { it.isBefore(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }
    }

    fun getClosestFutureTime(
        currentTime: LocalDateTime,
        times: List<LocalDateTime?>
    ): LocalDateTime? {
        return times.filterNotNull().filter { it.isAfter(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }
    }

    fun getClosestTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().minByOrNull { Duration.between(it, currentTime).abs() }
    }

    fun getClosestFutureTime(
        currentTime: ZonedDateTime,
        times: List<ZonedDateTime?>
    ): ZonedDateTime? {
        return times.filterNotNull().filter { it.isAfter(currentTime) }
            .minByOrNull { Duration.between(it, currentTime).abs() }
    }

}