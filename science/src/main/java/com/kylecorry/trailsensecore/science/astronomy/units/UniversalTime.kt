package com.kylecorry.trailsensecore.science.astronomy.units

import com.kylecorry.andromeda.core.time.toUTCLocal
import com.kylecorry.andromeda.core.time.utc
import com.kylecorry.trailsensecore.time.TimeUtils.toDecimal
import com.kylecorry.trailsensecore.time.TimeUtils.toDuration
import com.kylecorry.trailsensecore.math.MathUtils
import java.time.*
import kotlin.math.floor

typealias UniversalTime = LocalDateTime

/**
 * Julian centuries since 2000
 */
fun UniversalTime.toJulianCenturies(): Double {
    return (toJulianDay() - 2451545.0) / 36525.0
}

fun UniversalTime.toJulianDay(): Double {
    var Y = year.toDouble()
    var M = month.value.toDouble()
    val D =
        dayOfMonth.toDouble() + toLocalTime().toDuration().toDecimal() / 24.0

    if (M <= 2) {
        Y--
        M += 12
    }

    val A = floor(Y / 100)
    val B = 2 - A + floor(A / 4)

    return floor(365.25 * (Y + 4716)) + floor(30.6001 * (M + 1)) + D + B - 1524.5
}

internal fun UniversalTime.toSiderealTime(): GreenwichSiderealTime {
    val jd = UniversalTime.of(toLocalDate(), LocalTime.MIN).toJulianDay()
    val jd0 = UniversalTime.of(year, 1, 1, 0, 0).toJulianDay() - 1

    val days = jd - jd0

    val t = (jd0 - 2415020.0) / 36525.0

    val r = MathUtils.polynomial(t, 6.6460656, 2400.051262, 0.00002581)

    val b = 24 - r + 24 * (year - 1900)

    val t0 = 0.0657098 * days - b

    val ut = toLocalTime().toDuration().toDecimal()

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