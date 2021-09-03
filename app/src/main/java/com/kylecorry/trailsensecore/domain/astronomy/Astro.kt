package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.math.*
import com.kylecorry.andromeda.core.time.plusHours
import com.kylecorry.andromeda.core.time.toUTCLocal
import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.eclipse.LunarEclipseParameters
import com.kylecorry.trailsensecore.domain.astronomy.locators.ICelestialLocator
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonPhase
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.domain.astronomy.units.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

// Algorithms from Jean Meeus (Astronomical Algorithms 2nd Edition)
internal object Astro {

    fun getAltitude(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate,
        withRefraction: Boolean = false
        ): Float {
        val coords = locator.getCoordinates(ut)
        val horizon = HorizonCoordinate.fromEquatorial(coords, ut, location)
        return horizon.let {
            if (withRefraction) {
                it.withRefraction()
            } else {
                it
            }
        }.altitude.toFloat()
    }

    fun getAzimuth(
        locator: ICelestialLocator,
        ut: UniversalTime,
        location: Coordinate
    ): Bearing {
        val coords = locator.getCoordinates(ut)
        val horizon = HorizonCoordinate.fromEquatorial(coords, ut, location)
        return Bearing(horizon.azimuth.toFloat())
    }

    fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
        return timeToDecimal(hours, minutes, seconds) * 15
    }

    fun timeToDecimal(hours: Number, minutes: Number, seconds: Number): Double {
        return hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
    }

    /**
     * Converts an angle to between 0 and 360 degrees
     */
    fun reduceAngleDegrees(angle: Double): Double {
        return wrap(angle, 0.0, 360.0)
    }

    fun power(x: Double, power: Int): Double {
        var total = 1.0
        for (i in 0 until abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 1 / total
        }

        return total
    }

    /**
     * Computes a polynomial
     * Ex. 1 + 2x + 5x^2 + x^4
     * polynomial(x, 1, 2, 5, 0, 1)
     */
    fun polynomial(x: Double, vararg coefs: Double): Double {
        var runningTotal = 0.0
        for (i in coefs.indices) {
            runningTotal += power(x, i) * coefs[i]
        }

        return runningTotal
    }

    fun interpolate(
        n: Double,
        y1: Double,
        y2: Double,
        y3: Double
    ): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 + (n / 2.0) * (a + b + n * c)
    }

    fun julianDay(date: LocalDateTime): Double {
        return date.toJulianDay()
    }

    fun utFromJulianDay(jd: Double): LocalDateTime {
        return fromJulianDay(jd)
    }

    /**
     * The time difference between TT and UT (TT - UT) in seconds
     */
    fun deltaT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year - 2100)
    }

    /**
     * Calculates the universal time
     */
    fun ut(time: ZonedDateTime): LocalDateTime {
        return time.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
    }

    fun utToLocal(ut: LocalDateTime, zone: ZoneId): ZonedDateTime {
        return ut.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone)
    }


    /**
     * Get the current phase of the moon
     * @return The moon phase
     */
    fun getMoonPhase(time: ZonedDateTime = ZonedDateTime.now()): MoonPhase {
        val phaseAngle = getMoonPhaseAngle(time)
        val illumination = getMoonIllumination(phaseAngle).toFloat()

        for (phase in MoonTruePhase.values()) {
            if (phase.startAngle <= phaseAngle && phase.endAngle >= phaseAngle) {
                return MoonPhase(phase, illumination)
            }

            // Handle new moon
            if (phase.startAngle >= phase.endAngle) {
                if (phase.startAngle <= phaseAngle || phase.endAngle >= phaseAngle) {
                    return MoonPhase(phase, illumination)
                }
            }
        }

        return MoonPhase(MoonTruePhase.New, illumination)
    }

    fun getMoonPhaseAngle(time: ZonedDateTime): Double {
        val JDE = julianDay(time.toUTCLocal())

        val T = (JDE - 2451545) / 36525.0

        val D =
            normalizeAngle(
                297.8501921 + 445267.1114034 * T - 0.0018819 * T.pow(
                    2
                ) + T.pow(3) / 545868 - T.pow(4) / 113065000
            )

        val M = normalizeAngle(
            357.5291092 + 35999.0502909 * T - 0.0001536 * T.pow(2) + T.pow(3) / 24490000
        )

        val Mp =
            normalizeAngle(
                134.9633964 + 477198.8675055 * T - 0.0087414 * T.pow(
                    2
                ) + T.pow(3) / 69699 - T.pow(4) / 14712000
            )

        val i =
            180 - D - 6.289 * sinDegrees(Mp) + 2.100 * sinDegrees(
                M
            ) - 1.274 * sinDegrees(2 * D - Mp) - 0.658 * sinDegrees(
                2 * D
            ) - 0.214 * sinDegrees(
                2 * Mp
            ) - 0.110 * sinDegrees(D)

        return (i + 180) % 360.0
    }

    fun getMoonIllumination(phaseAngle: Double): Double {
        return ((1 + cosDegrees(phaseAngle - 180)) / 2) * 100
    }

    fun getJDEOfMeanMoonPhase(k: Double): Double {
        val T = k / 1236.85
        return 2451550.09766 + 29.530588861 * k + 0.00015437 * power(T, 2) - 0.00000015 * power(
            T,
            3
        ) + 0.00000000074 * power(T, 4)
    }

    fun getNextLunarEclipse(ut: LocalDateTime): LunarEclipseParameters {
        var k = getNextMoonPhaseK(ut, MoonTruePhase.Full)
        var T: Double
        var F: Double
        do {
            T = k / 1236.85
            F = reduceAngleDegrees(
                160.7108 + 390.67050284 * k - 0.0016118 * power(T, 2) - 0.00000227 * power(
                    T,
                    3
                ) + 0.000000011 * power(T, 4)
            )
            if (sinDegrees(F).absoluteValue > 0.36) {
                k += 1
            }
        } while (sinDegrees(F).absoluteValue > 0.36)

        val mean = getJDEOfMeanMoonPhase(k)
        val M = reduceAngleDegrees(
            2.5534 + 29.1053567 * k - 0.0000014 * power(
                T,
                2
            ) - 0.00000011 * power(T, 3)
        )
        val MPrime = reduceAngleDegrees(
            201.5643 + 385.81693528 * k + 0.0107582 * power(T, 2) + 0.00001238 * power(
                T,
                3
            ) - 0.000000058 * power(T, 4)
        )
        val omega = reduceAngleDegrees(
            124.7746 - 1.56375588 * k + 0.0020672 * power(
                T,
                2
            ) + 0.00000215 * power(T, 3)
        )
        val E = polynomial(T, 1.0, -0.002516, -0.0000074)

        val F1 = F - 0.02665 * sinDegrees(omega)
        val A1 = 299.77 + 0.107408 * k - 0.009173 * power(T, 2)

        var correction = 0.0
        val table = table54_1()
        for (row in table) {
            correction += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * sinDegrees(
                row[2] * M + row[3] * MPrime + row[4] * F1 + row[5] * A1 + row[6] * omega
            )
        }

        val correctedJD = mean + correction

        var p = 0.0
        val tableP = table54_P()
        for (row in tableP) {
            p += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * sinDegrees(
                row[2] * M + row[3] * MPrime + row[4] * F1
            )
        }

        var q = 0.0
        val tableQ = table54_Q()
        for (row in tableQ) {
            q += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * cosDegrees(
                row[2] * M + row[3] * MPrime
            )
        }

        val W = cosDegrees(F1).absoluteValue

        val gamma = (p * cosDegrees(F1) + q * sinDegrees(F1)) * (1 - 0.0048 * W)
        val u =
            0.0059 + 0.0046 * E * cosDegrees(M) - 0.0182 * cosDegrees(MPrime) + 0.0004 * cosDegrees(
                2 * MPrime
            ) - 0.0005 * cosDegrees(M + MPrime)

        val n = 0.5458 + 0.04 * cosDegrees(MPrime)

        val datetime = ZonedDateTime.of(utFromJulianDay(correctedJD), ZoneId.of("UTC"))

        return LunarEclipseParameters(
            datetime.toInstant().minusSeconds(deltaT(datetime.year).toLong()),
            gamma,
            u,
            n
        )

    }

    fun getNextMoonPhaseK(date: LocalDateTime, moonTruePhase: MoonTruePhase): Double {
        val year = date.year
        val day = date.dayOfYear / 365.25
        val hour = (date.hour / 24.0) / 365.25
        val minute = (((date.minute / 60.0) / 24.0) / 365.25)
        val y = year + day + hour + minute
        val k = (y - 2000) * 12.3685

        val ending = when (moonTruePhase) {
            MoonTruePhase.New -> 0.0
            MoonTruePhase.WaningCrescent -> 0.125
            MoonTruePhase.ThirdQuarter -> 0.25
            MoonTruePhase.WaningGibbous -> 0.375
            MoonTruePhase.Full -> 0.5
            MoonTruePhase.WaxingGibbous -> 0.625
            MoonTruePhase.FirstQuarter -> 0.75
            MoonTruePhase.WaxingCrescent -> 0.875
        }

        val intK = floor(k)
        val remainder = k % 1

        return if (remainder > ending) {
            intK + 1.0 + ending
        } else {
            intK + ending
        }
    }

    fun cube(a: Double): Double {
        return a * a * a
    }

    fun square(a: Double): Double {
        return a * a
    }


    fun ut0hOnDate(date: ZonedDateTime): LocalDateTime {
        val localDate = date.toLocalDate()

        for (i in -1..1) {
            val ut0h = ut(date.plusDays(i.toLong())).toLocalDate().atStartOfDay()
            val local0h = utToLocal(ut0h, date.zone)
            if (localDate == local0h.toLocalDate()) {
                return ut0h
            }
        }

        return ut(date).toLocalDate().atStartOfDay()
    }

    private fun table54_1(): Array<Array<Int>> {
        return arrayOf(
            // [term, E bool, M, Mprime, F prime, A prime, omega]
            arrayOf(-4065, 0, 0, 1, 0, 0, 0),
            arrayOf(1727, 1, 1, 0, 0, 0, 0),
            arrayOf(161, 0, 0, 2, 0, 0, 0),
            arrayOf(-97, 0, 0, 0, 2, 0, 0),
            arrayOf(73, 1, -1, 1, 0, 0, 0),
            arrayOf(-50, 1, 1, 1, 0, 0, 0),
            arrayOf(-23, 0, 0, 1, -2, 0, 0),
            arrayOf(21, 1, 2, 0, 0, 0, 0),
            arrayOf(12, 0, 0, 1, 2, 0, 0),
            arrayOf(6, 1, 1, 2, 0, 0, 0),
            arrayOf(-4, 0, 0, 3, 0, 0, 0),
            arrayOf(-3, 1, 1, 0, 2, 0, 0),
            arrayOf(3, 0, 0, 0, 0, 1, 0),
            arrayOf(-2, 1, 1, 0, -2, 0, 0),
            arrayOf(-2, 1, -1, 2, 0, 0, 0),
            arrayOf(-2, 0, 0, 0, 0, 0, 1),
        )
    }

    private fun table54_P(): Array<Array<Int>> {
        return arrayOf(
            // [term, E bool, M, Mprime, F prime]
            arrayOf(2070, 1, 1, 0, 0),
            arrayOf(24, 1, 2, 0, 0),
            arrayOf(-392, 0, 0, 1, 0),
            arrayOf(116, 0, 0, 2, 0),
            arrayOf(-73, 1, 1, 1, 0),
            arrayOf(67, 1, -1, 1, 0),
            arrayOf(118, 0, 0, 0, 2),
        )
    }

    private fun table54_Q(): Array<Array<Int>> {
        return arrayOf(
            // [term, E bool, M, Mprime]
            arrayOf(52207, 0, 0, 0),
            arrayOf(-48, 1, 1, 0),
            arrayOf(20, 1, 2, 0),
            arrayOf(-3299, 0, 0, 1),
            arrayOf(-60, 1, 1, 1),
            arrayOf(41, 1, -1, 1),
        )
    }

}