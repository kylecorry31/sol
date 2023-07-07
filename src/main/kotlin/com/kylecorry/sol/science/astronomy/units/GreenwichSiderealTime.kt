package com.kylecorry.sol.science.astronomy.units

import java.time.LocalDate

internal class GreenwichSiderealTime(_hours: Double) : SiderealTime(_hours, 0.0) {

    fun toUniversalTime(date: LocalDate): UniversalTime {
        val jd = date.atStartOfDay().toJulianDay()
        val jd0 = UniversalTime.of(date.year, 1, 1, 0, 0).toJulianDay() - 1
        val days = jd - jd0
        val t = (jd0 - 2415020) / 36525.0
        val r = com.kylecorry.sol.math.SolMath.polynomial(t, 6.6460656, 2400.051262, 0.00002581)
        val b = 24 - r + 24 * (date.year - 1900)
        var t0 = 0.0657098 * days - b

        if (t0 < 0) {
            t0 += 24
        } else if (t0 > 24) {
            t0 -= 24
        }

        var a = hours - t0

        if (a < 0) {
            a += 24
        }

        val ut = 0.99727 * a

        val duration = com.kylecorry.sol.time.Time.hours(ut)
        return UniversalTime.of(date, duration.toLocalTime())
    }
}