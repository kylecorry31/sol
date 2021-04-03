package com.kylecorry.trailsensecore.domain.time

import java.time.*

fun LocalDateTime.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.of(this, ZoneId.systemDefault())
}

fun LocalDateTime.toEpochMillis(): Long {
    return this.toZonedDateTime().toEpochSecond() * 1000
}

fun ZonedDateTime.toUTCLocal(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
}

fun ZonedDateTime.atStartOfDay(): ZonedDateTime {
    return ZonedDateTime.of(this.toLocalDate(), LocalTime.MIN, this.zone)
}

fun LocalDateTime.roundNearestMinute(minutes: Long): LocalDateTime {
    val minute = this.minute
    val newMinute = (minute / minutes) * minutes

    val diff = newMinute - minute
    return this.plusMinutes(diff)
}

fun Instant.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
}

fun LocalDateTime.plusHours(hours: Double): LocalDateTime {
    val h = hours.toLong()
    val m = (hours % 1) * 60
    val s = (m % 1) * 60
    val ns = (1e9 * s).toLong()
    return this.plusHours(h).plusMinutes(m.toLong()).plusNanos(ns)
}

fun duration(hours: Long = 0L, minutes: Long = 0L, seconds: Long = 0L): Duration {
    return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
}