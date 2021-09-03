package com.kylecorry.trailsensecore.domain.astronomy.units

import com.kylecorry.andromeda.core.math.toRadians
import java.time.Duration
import java.time.LocalTime

object TimeUtils {

    fun LocalTime.toDuration(): Duration {
        return Duration.ofHours(hour.toLong()).plusMinutes(minute.toLong())
            .plusSeconds(second.toLong()).plusNanos(nano.toLong())
    }

    fun Duration.toLocalTime(): LocalTime {
        return LocalTime.MIN.plus(this)
    }

    fun Duration.toDecimal(): Double {
        val millis = toMillis()
        val seconds = millis / 1000.0
        val minutes = seconds / 60.0
        return minutes / 60.0
    }

    fun Duration.toDegrees(): Double {
        return toDecimal() * 15.0
    }

    fun Duration.toRadians(): Double {
        return toDegrees().toRadians()
    }

    fun degreesToTime(degrees: Double): Duration {
        return decimalToTime(degrees * 15)
    }

    fun decimalToTime(hours: Double): Duration {
        val minutes = hours * 60
        val seconds = minutes * 60
        val millis = seconds * 1000
        return Duration.ofMillis(millis.toLong())
    }
}

/*
    LCT (Local Civil Time) - the local time of the user (using timezones)
        ZonedDateTime
    UT (Universal Time) - the time in UTC / Greenwich time zone
        LocalDateTime
    TT (Terrestrial Time) - UT with rotation of Earth correction
        LocalDateTime
    GST (Greenwich Sidereal Time) - star time at Greenwich
        Double
    LST (Local Sidereal Time) - star time at local
        Double
 */
