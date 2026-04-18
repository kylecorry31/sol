package com.kylecorry.sol.science.astronomy.eclipse.solar

import com.kylecorry.sol.math.algebra.Algebra
import com.kylecorry.sol.math.arithmetic.Arithmetic.power
import com.kylecorry.sol.math.trigonometry.Trigonometry
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.sol.science.astronomy.locators.Moon
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.fromJulianDay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

internal class SolarEclipseParameterProvider {
    private val moon = Moon()

    fun getNextSolarEclipseParameters(after: Instant): SolarEclipseParameters {
        val ut = ZonedDateTime.ofInstant(after, ZoneId.of("UTC")).toLocalDateTime()
        return getNextSolarEclipse(ut)
    }

    private fun getNextSolarEclipse(ut: UniversalTime): SolarEclipseParameters {
        var k = moon.getNextPhaseK(ut, MoonTruePhase.New)
        var julianCenturiesSinceJ2000: Double
        var F: Double
        var iterations = 0
        do {
            julianCenturiesSinceJ2000 = k / 1236.85
            F = Trigonometry.normalizeAngle(
                160.7108 + 390.67050284 * k - 0.0016118 * power(julianCenturiesSinceJ2000, 2) - 0.00000227 * power(
                    julianCenturiesSinceJ2000,
                    3
                ) + 0.000000011 * power(julianCenturiesSinceJ2000, 4)
            )
            if (sinDegrees(F).absoluteValue > 0.36) {
                k += 1
            }
            iterations++
        } while (sinDegrees(F).absoluteValue > 0.36 && iterations < MAX_PHASE_SEARCH_ITERATIONS)

        check(sinDegrees(F).absoluteValue <= 0.36) {
            "Solar eclipse phase search did not converge within $MAX_PHASE_SEARCH_ITERATIONS iterations"
        }

        val mean = getJDEOfMeanMoonPhase(k)
        val M = Trigonometry.normalizeAngle(
            2.5534 + 29.1053567 * k - 0.0000014 * power(
                julianCenturiesSinceJ2000,
                2
            ) - 0.00000011 * power(julianCenturiesSinceJ2000, 3)
        )
        val MPrime = Trigonometry.normalizeAngle(
            201.5643 + 385.81693528 * k + 0.0107582 * power(julianCenturiesSinceJ2000, 2) + 0.00001238 * power(
                julianCenturiesSinceJ2000,
                3
            ) - 0.000000058 * power(julianCenturiesSinceJ2000, 4)
        )
        val omega = Trigonometry.normalizeAngle(
            124.7746 - 1.56375588 * k + 0.0020672 * power(
                julianCenturiesSinceJ2000,
                2
            ) + 0.00000215 * power(julianCenturiesSinceJ2000, 3)
        )
        val E = Algebra.polynomial(julianCenturiesSinceJ2000, 1.0, -0.002516, -0.0000074)

        val F1 = F - 0.02665 * sinDegrees(omega)
        val A1 = 299.77 + 0.107408 * k - 0.009173 * power(julianCenturiesSinceJ2000, 2)

        var correction = 0.0
        val table = table54Part1()
        for (row in table) {
            correction += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * sinDegrees(
                row[2] * M + row[3] * MPrime + row[4] * F1 + row[5] * A1 + row[6] * omega
            )
        }

        val correctedJD = mean + correction

        var p = 0.0
        val tableP = table54PTerms()
        for (row in tableP) {
            p += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else E) * sinDegrees(
                row[2] * M + row[3] * MPrime + row[4] * F1
            )
        }

        var q = 0.0
        val tableQ = table54QTerms()
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


        val datetime = ZonedDateTime.of(fromJulianDay(correctedJD), ZoneId.of("UTC"))

        return SolarEclipseParameters(
            datetime.toInstant().minusSeconds(TerrestrialTime.getDeltaT(datetime.year).toLong()),
            gamma,
            u
        )

    }

    private fun getJDEOfMeanMoonPhase(k: Double): Double {
        val julianCenturiesSinceJ2000 = k / 1236.85
        return 2451550.09766 + 29.530588861 * k + 0.00015437 * power(julianCenturiesSinceJ2000, 2) - 0.00000015 * power(
            julianCenturiesSinceJ2000,
            3
        ) + 0.00000000074 * power(julianCenturiesSinceJ2000, 4)
    }

    private fun table54Part1(): Array<IntArray> {
        return arrayOf(
            // [term, E bool, M, Mprime, F prime, A prime, omega]
            intArrayOf(-4075, 0, 0, 1, 0, 0, 0),
            intArrayOf(1721, 1, 1, 0, 0, 0, 0),
            intArrayOf(161, 0, 0, 2, 0, 0, 0),
            intArrayOf(-97, 0, 0, 0, 2, 0, 0),
            intArrayOf(73, 1, -1, 1, 0, 0, 0),
            intArrayOf(-50, 1, 1, 1, 0, 0, 0),
            intArrayOf(-23, 0, 0, 1, -2, 0, 0),
            intArrayOf(21, 1, 2, 0, 0, 0, 0),
            intArrayOf(12, 0, 0, 1, 2, 0, 0),
            intArrayOf(6, 1, 1, 2, 0, 0, 0),
            intArrayOf(-4, 0, 0, 3, 0, 0, 0),
            intArrayOf(-3, 1, 1, 0, 2, 0, 0),
            intArrayOf(3, 0, 0, 0, 0, 1, 0),
            intArrayOf(-2, 1, 1, 0, -2, 0, 0),
            intArrayOf(-2, 1, -1, 2, 0, 0, 0),
            intArrayOf(-2, 0, 0, 0, 0, 0, 1),
        )
    }

    private fun table54PTerms(): Array<IntArray> {
        return arrayOf(
            // [term, E bool, M, Mprime, F prime]
            intArrayOf(2070, 1, 1, 0, 0),
            intArrayOf(24, 1, 2, 0, 0),
            intArrayOf(-392, 0, 0, 1, 0),
            intArrayOf(116, 0, 0, 2, 0),
            intArrayOf(-73, 1, 1, 1, 0),
            intArrayOf(67, 1, -1, 1, 0),
            intArrayOf(118, 0, 0, 0, 2),
        )
    }

    private fun table54QTerms(): Array<IntArray> {
        return arrayOf(
            // [term, E bool, M, Mprime]
            intArrayOf(52207, 0, 0, 0),
            intArrayOf(-48, 1, 1, 0),
            intArrayOf(20, 1, 2, 0),
            intArrayOf(-3299, 0, 0, 1),
            intArrayOf(-60, 1, 1, 1),
            intArrayOf(41, 1, -1, 1),
        )
    }

    companion object {
        private const val MAX_PHASE_SEARCH_ITERATIONS = 100
    }
}
