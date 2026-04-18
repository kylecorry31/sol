package com.kylecorry.sol.science.astronomy.eclipse.lunar

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

internal class LunarEclipseParameterProvider {

    private val moon = Moon()

    fun getNextLunarEclipseParameters(after: Instant): LunarEclipseParameters {
        val ut = ZonedDateTime.ofInstant(after, ZoneId.of("UTC")).toLocalDateTime()
        return getNextLunarEclipse(ut)
    }

    private fun getNextLunarEclipse(ut: UniversalTime): LunarEclipseParameters {
        var k = moon.getNextPhaseK(ut, MoonTruePhase.Full)
        var julianCenturiesSinceJ2000: Double
        var argumentOfLatitude: Double
        var iterations = 0
        do {
            julianCenturiesSinceJ2000 = k / 1236.85
            argumentOfLatitude = Trigonometry.normalizeAngle(
                160.7108 + 390.67050284 * k - 0.0016118 * power(julianCenturiesSinceJ2000, 2) - 0.00000227 * power(
                    julianCenturiesSinceJ2000,
                    3
                ) + 0.000000011 * power(julianCenturiesSinceJ2000, 4)
            )
            if (sinDegrees(argumentOfLatitude).absoluteValue > 0.36) {
                k += 1
            }
            iterations++
        } while (sinDegrees(argumentOfLatitude).absoluteValue > 0.36 && iterations < MAX_PHASE_SEARCH_ITERATIONS)

        check(sinDegrees(argumentOfLatitude).absoluteValue <= 0.36) {
            "Lunar eclipse phase search did not converge within $MAX_PHASE_SEARCH_ITERATIONS iterations"
        }

        val meanJulianDay = getJDEOfMeanMoonPhase(k)
        val sunMeanAnomaly = Trigonometry.normalizeAngle(
            2.5534 + 29.1053567 * k - 0.0000014 * power(
                julianCenturiesSinceJ2000,
                2
            ) - 0.00000011 * power(julianCenturiesSinceJ2000, 3)
        )
        val moonMeanAnomaly = Trigonometry.normalizeAngle(
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
        val eccentricity = moon.getEccentricity(julianCenturiesSinceJ2000)

        val adjustedArgumentOfLatitude = argumentOfLatitude - 0.02665 * sinDegrees(omega)
        // The astronomical algorithms book does not specify what this adjustment is for
        val adjustment1 = 299.77 + 0.107408 * k - 0.009173 * power(julianCenturiesSinceJ2000, 2)

        var correction = 0.0
        val table = table54Part1()
        for (row in table) {
            correction += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else eccentricity) * sinDegrees(
                row[2] * sunMeanAnomaly +
                        row[3] * moonMeanAnomaly +
                        row[4] * adjustedArgumentOfLatitude +
                        row[5] * adjustment1 +
                        row[6] * omega
            )
        }

        val correctedJulianDay = meanJulianDay + correction

        var p = 0.0
        val tableP = table54PTerms()
        for (row in tableP) {
            p += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else eccentricity) * sinDegrees(
                row[2] * sunMeanAnomaly + row[3] * moonMeanAnomaly + row[4] * adjustedArgumentOfLatitude
            )
        }

        var q = 0.0
        val tableQ = table54QTerms()
        for (row in tableQ) {
            q += (row[0] / 10000.0) * (if (row[1] == 0) 1.0 else eccentricity) * cosDegrees(
                row[2] * sunMeanAnomaly + row[3] * moonMeanAnomaly
            )
        }

        val cosineArgumentOfLatitude = cosDegrees(adjustedArgumentOfLatitude).absoluteValue

        val minDistanceFromCenter = (p * cosDegrees(adjustedArgumentOfLatitude) +
                q * sinDegrees(adjustedArgumentOfLatitude)) * (1 - 0.0048 * cosineArgumentOfLatitude)
        val umbralConeRadius = 0.0059 +
                0.0046 * eccentricity * cosDegrees(sunMeanAnomaly) -
                0.0182 * cosDegrees(moonMeanAnomaly) +
                0.0004 * cosDegrees(2 * moonMeanAnomaly) -
                0.0005 * cosDegrees(sunMeanAnomaly + moonMeanAnomaly)

        val n = 0.5458 + 0.04 * cosDegrees(moonMeanAnomaly)

        val datetime = ZonedDateTime.of(fromJulianDay(correctedJulianDay), ZoneId.of("UTC"))

        return LunarEclipseParameters(
            datetime.toInstant().minusSeconds(TerrestrialTime.getDeltaT(datetime.year).toLong()),
            minDistanceFromCenter,
            umbralConeRadius,
            n
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
            intArrayOf(-4065, 0, 0, 1, 0, 0, 0),
            intArrayOf(1727, 1, 1, 0, 0, 0, 0),
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
