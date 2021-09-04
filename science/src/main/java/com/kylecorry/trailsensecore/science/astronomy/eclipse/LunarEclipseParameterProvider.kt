package com.kylecorry.trailsensecore.science.astronomy.eclipse

import com.kylecorry.andromeda.core.math.cosDegrees
import com.kylecorry.andromeda.core.math.power
import com.kylecorry.andromeda.core.math.sinDegrees
import com.kylecorry.trailsensecore.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.trailsensecore.science.astronomy.locators.Moon
import com.kylecorry.trailsensecore.science.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.science.astronomy.units.UniversalTime
import com.kylecorry.trailsensecore.science.astronomy.units.fromJulianDay
import com.kylecorry.trailsensecore.math.MathUtils
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

internal class LunarEclipseParameterProvider {

    private val moon = Moon()

    fun getNextLunarEclipseParameters(after: Instant): LunarEclipseParameters {
        val ut = ZonedDateTime.ofInstant(after, ZoneId.of("UTC")).toLocalDateTime()
        return getNextLunarEclipse(ut)
    }

    private fun getNextLunarEclipse(ut: UniversalTime): LunarEclipseParameters {
        var k = moon.getNextPhaseK(ut, MoonTruePhase.Full)
        var T: Double
        var F: Double
        do {
            T = k / 1236.85
            F = MathUtils.reduceAngleDegrees(
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
        val M = MathUtils.reduceAngleDegrees(
            2.5534 + 29.1053567 * k - 0.0000014 * power(
                T,
                2
            ) - 0.00000011 * power(T, 3)
        )
        val MPrime = MathUtils.reduceAngleDegrees(
            201.5643 + 385.81693528 * k + 0.0107582 * power(T, 2) + 0.00001238 * power(
                T,
                3
            ) - 0.000000058 * power(T, 4)
        )
        val omega = MathUtils.reduceAngleDegrees(
            124.7746 - 1.56375588 * k + 0.0020672 * power(
                T,
                2
            ) + 0.00000215 * power(T, 3)
        )
        val E = MathUtils.polynomial(T, 1.0, -0.002516, -0.0000074)

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

        val datetime = ZonedDateTime.of(fromJulianDay(correctedJD), ZoneId.of("UTC"))

        return LunarEclipseParameters(
            datetime.toInstant().minusSeconds(TerrestrialTime.getDeltaT(datetime.year).toLong()),
            gamma,
            u,
            n
        )

    }

    private fun getJDEOfMeanMoonPhase(k: Double): Double {
        val T = k / 1236.85
        return 2451550.09766 + 29.530588861 * k + 0.00015437 * power(T, 2) - 0.00000015 * power(
            T,
            3
        ) + 0.00000000074 * power(T, 4)
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