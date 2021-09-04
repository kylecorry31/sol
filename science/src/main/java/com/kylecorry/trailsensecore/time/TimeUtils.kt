package com.kylecorry.trailsensecore.time

import com.kylecorry.trailsensecore.math.TSMath.toRadians
import com.kylecorry.trailsensecore.science.astronomy.units.UniversalTime
import com.kylecorry.trailsensecore.science.astronomy.units.atZeroHour
import com.kylecorry.trailsensecore.science.astronomy.units.toUniversalTime
import java.time.*

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

    fun hmsToTime(hours: Int, minutes: Int, seconds: Number): Duration {
        val h = hours + minutes / 60.0 + seconds.toDouble() / 3600.0
        return decimalToTime(h)
    }

    fun degreesToTime(degrees: Double): Duration {
        return decimalToTime(degrees / 15)
    }

    fun dmsToTime(degrees: Int, minutes: Int, seconds: Number): Duration {
        val d = degrees + minutes / 60.0 + seconds.toDouble() / 3600.0
        return degreesToTime(d)
    }

    fun decimalToTime(hours: Double): Duration {
        val minutes = hours * 60
        val seconds = minutes * 60
        val millis = seconds * 1000
        return Duration.ofMillis(millis.toLong())
    }

    fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
        return timeToDecimal(hours, minutes, seconds) * 15
    }

    fun timeToDecimal(hours: Number, minutes: Number, seconds: Number): Double {
        return hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
    }

    fun UniversalTime.toLocal(zone: ZoneId): ZonedDateTime {
        return atZone(ZoneId.of("UTC")).withZoneSameInstant(zone)
    }

    fun ut0hOnDate(date: ZonedDateTime): LocalDateTime {
        val localDate = date.toLocalDate()

        for (i in -1..1) {
            val ut0h = date.plusDays(i.toLong()).toUniversalTime().atZeroHour()
            val local0h = ut0h.toLocal(date.zone)
            if (localDate == local0h.toLocalDate()) {
                return ut0h
            }
        }

        return date.toUniversalTime().atZeroHour()
    }

    fun ZonedDateTime.atStartOfDay(): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDate(), LocalTime.MIN, this.zone)
    }

    fun LocalDateTime.plusHours(hours: Double): LocalDateTime {
        val h = hours.toLong()
        val m = (hours % 1) * 60
        val s = (m % 1) * 60
        val ns = (1e9 * s).toLong()
        return this.plusHours(h).plusMinutes(m.toLong()).plusNanos(ns)
    }

    fun ZonedDateTime.toUTCLocal(): LocalDateTime {
        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
    }

    fun Instant.utc(): ZonedDateTime {
        return ZonedDateTime.ofInstant(this, ZoneId.of("UTC"))
    }

    fun duration(hours: Long = 0L, minutes: Long = 0L, seconds: Long = 0L): Duration {
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
    }

}