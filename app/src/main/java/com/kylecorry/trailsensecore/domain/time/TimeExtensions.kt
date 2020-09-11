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

fun LocalDateTime.roundNearestMinute(minutes: Long): LocalDateTime {
    val minute = this.minute
    val newMinute = (minute / minutes) * minutes

    val diff = newMinute - minute
    return this.plusMinutes(diff)
}

fun Instant.toZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
}