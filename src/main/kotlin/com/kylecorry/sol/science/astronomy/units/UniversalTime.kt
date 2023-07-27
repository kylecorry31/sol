package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.time.Time.toUTCLocal
import com.kylecorry.sol.time.Time.utc
import java.time.*
import kotlin.math.floor

typealias UniversalTime = LocalDateTime

/**
 * Julian centuries since 2000
 */
fun UniversalTime.toJulianCenturies(): Double {
    return (toJulianDay() - 2451545.0) / 36525.0
}

fun UniversalTime.toJulianDay(includeTime: Boolean = true): Double {
    var Y = year.toDouble()
    var M = month.value.toDouble()
    val D =
        dayOfMonth.toDouble() + if (includeTime) toLocalTime().toDecimalHours() / 24.0 else 0.0

    if (M <= 2) {
        Y--
        M += 12
    }

    val A = floor(Y / 100)
    val B = 2 - A + floor(A / 4)

    return floor(365.25 * (Y + 4716)) + floor(30.6001 * (M + 1)) + D + B - 1524.5
}

internal fun UniversalTime.jd0(): Double {
    val Y = (year - 1).toDouble()
    val A = floor(Y / 100)
    val B = 2 - A + floor(A / 4)

    return floor(365.25 * (Y + 4716)) + B - 1094.5 // Regular julian day - 1
}

internal fun UniversalTime.toSiderealTime(): GreenwichSiderealTime {
    val jd = toJulianDay(false)
    val jd0 = jd0()

    val days = jd - jd0

    val t = (jd0 - 2415020.0) / 36525.0

    val r = SolMath.polynomial(t, 6.6460656, 2400.051262, 0.00002581)

    val b = 24 - r + 24 * (year - 1900)

    val t0 = 0.0657098 * days - b

    val ut = toLocalTime().toDecimalHours()

    val gst = t0 + 1.002738 * ut

    return GreenwichSiderealTime(gst)
}

fun ZonedDateTime.toUniversalTime(): UniversalTime {
    return toUTCLocal()
}

fun UniversalTime.atZeroHour(): UniversalTime {
    return toLocalDate().atStartOfDay()
}

fun Instant.toUniversalTime(): UniversalTime {
    return utc().toUniversalTime()
}

fun fromJulianDay(jd: Double): UniversalTime {
    val f = (jd + 0.5) % 1
    val z = (jd + 0.5) - f

    val a = if (z < 2299161) {
        z
    } else {
        val alpha = floor((z - 1867216.25) / 36524.25)
        z + 1 + alpha - floor(alpha / 4)
    }

    val b = a + 1524
    val c = floor((b - 122.1) / 365.25)
    val d = floor(365.25 * c)
    val e = floor((b - d) / 30.6001)
    val day = b - d - floor(30.6001 * e) + f

    val dayOfMonth = floor(day).toInt()
    val hours = (day - dayOfMonth) * 24
    val hour = floor(hours).toInt()
    val minutes = (hours - hour) * 60
    val minute = floor(minutes).toInt()
    val seconds = floor((minutes - minute) * 60).toInt()
    val month = if (e < 14) {
        e - 1
    } else {
        e - 13
    }.toInt()

    val year = if (month > 2) {
        c - 4716
    } else {
        c - 4715
    }.toInt()

    return LocalDateTime.of(year, month, dayOfMonth, hour, minute, seconds)
}

fun UniversalTime.toLocal(zone: ZoneId): ZonedDateTime {
    return atZone(ZoneId.of("UTC")).withZoneSameInstant(zone)
}

fun UniversalTime.toInstant(): Instant {
    return atZone(ZoneId.of("UTC")).toInstant()
}

fun ut0hOnDate(date: ZonedDateTime): UniversalTime {
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

fun LocalTime.toDuration(): Duration {
    return Duration.ofHours(hour.toLong()).plusMinutes(minute.toLong())
        .plusSeconds(second.toLong()).plusNanos(nano.toLong())
}

fun LocalTime.toDecimalHours(): Double {
    val hours = hour.toDouble()
    val minutes = minute.toDouble() / 60.0
    val seconds = second.toDouble() / 3600.0
    val nanos = nano.toDouble() / 3600.0 / 1_000_000_000.0
    return hours + minutes + seconds + nanos
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
    return Time.hours(degrees / 15)
}

fun dmsToTime(degrees: Int, minutes: Int, seconds: Number): Duration {
    val d = degrees + minutes / 60.0 + seconds.toDouble() / 3600.0
    return degreesToTime(d)
}

fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
    return timeToDecimal(hours, minutes, seconds) * 15
}

fun timeToDecimal(hours: Number, minutes: Number, seconds: Number): Double {
    return hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
}