package com.kylecorry.sol.science.astronomy.moon

import com.kylecorry.sol.math.algebra.Algebra.polynomial
import com.kylecorry.sol.math.trigonometry.Trigonometry
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.fromJulianDay

internal object MoonPhaseTimeCalculator {

    const val SYNODIC_MONTH_DAYS = 29.530588861

    fun getParameters(k: Double): MoonPhaseTimeParameters {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t
        return MoonPhaseTimeParameters(
            k,
            t,
            getMeanPhaseJulianEphemerisDay(k, t),
            polynomial(t, 1.0, -0.002516, -0.0000074),
            Trigonometry.normalizeAngle(2.5534 + 29.1053567 * k - 0.0000014 * t2 - 0.00000011 * t3),
            Trigonometry.normalizeAngle(
                201.5643 + 385.81693528 * k + 0.0107582 * t2 + 0.00001238 * t3 - 0.000000058 * t4
            ),
            Trigonometry.normalizeAngle(
                160.7108 + 390.67050284 * k - 0.0016118 * t2 - 0.00000227 * t3 + 0.000000011 * t4
            ),
            Trigonometry.normalizeAngle(124.7746 - 1.56375588 * k + 0.0020672 * t2 + 0.00000215 * t3)
        )
    }

    fun getMeanPhaseJulianEphemerisDay(k: Double): Double {
        return getMeanPhaseJulianEphemerisDay(k, k / 1236.85)
    }

    fun getNewMoon(k: Double): UniversalTime {
        val parameters = getParameters(k)
        val eccentricity = parameters.eccentricity
        val sunMeanAnomaly = parameters.sunMeanAnomaly
        val moonMeanAnomaly = parameters.moonMeanAnomaly
        val argumentOfLatitude = parameters.argumentOfLatitude

        val correction =
            -0.40720 * sinDegrees(moonMeanAnomaly) +
                    0.17241 * eccentricity * sinDegrees(sunMeanAnomaly) +
                    0.01608 * sinDegrees(2 * moonMeanAnomaly) +
                    0.01039 * sinDegrees(2 * argumentOfLatitude) +
                    0.00739 * eccentricity * sinDegrees(moonMeanAnomaly - sunMeanAnomaly) -
                    0.00514 * eccentricity * sinDegrees(moonMeanAnomaly + sunMeanAnomaly) +
                    0.00208 * eccentricity * eccentricity * sinDegrees(2 * sunMeanAnomaly) -
                    0.00111 * sinDegrees(moonMeanAnomaly - 2 * argumentOfLatitude) -
                    0.00057 * sinDegrees(moonMeanAnomaly + 2 * argumentOfLatitude) +
                    0.00056 * eccentricity * sinDegrees(2 * moonMeanAnomaly + sunMeanAnomaly) -
                    0.00042 * sinDegrees(3 * moonMeanAnomaly) +
                    0.00042 * eccentricity * sinDegrees(sunMeanAnomaly + 2 * argumentOfLatitude) +
                    0.00038 * eccentricity * sinDegrees(sunMeanAnomaly - 2 * argumentOfLatitude) -
                    0.00024 * eccentricity * sinDegrees(2 * moonMeanAnomaly - sunMeanAnomaly) -
                    0.00017 * sinDegrees(parameters.ascendingNode) -
                    0.00007 * sinDegrees(moonMeanAnomaly + 2 * sunMeanAnomaly) +
                    0.00004 * sinDegrees(2 * moonMeanAnomaly - 2 * argumentOfLatitude) +
                    0.00004 * sinDegrees(3 * sunMeanAnomaly) +
                    0.00003 * sinDegrees(moonMeanAnomaly + sunMeanAnomaly - 2 * argumentOfLatitude) +
                    0.00003 * sinDegrees(2 * moonMeanAnomaly + 2 * argumentOfLatitude) -
                    0.00003 * sinDegrees(moonMeanAnomaly + sunMeanAnomaly + 2 * argumentOfLatitude) +
                    0.00003 * sinDegrees(moonMeanAnomaly - sunMeanAnomaly + 2 * argumentOfLatitude) -
                    0.00002 * sinDegrees(moonMeanAnomaly - sunMeanAnomaly - 2 * argumentOfLatitude) -
                    0.00002 * sinDegrees(3 * moonMeanAnomaly + sunMeanAnomaly) +
                    0.00002 * sinDegrees(4 * moonMeanAnomaly)

        val t2 = parameters.julianCenturiesSinceJ2000 * parameters.julianCenturiesSinceJ2000
        val planetaryCorrection = CORRECTION_PLANETARY_TERMS.sumOf {
            it[0] * sinDegrees(it[1] + it[2] * k + it[3] * t2)
        }

        val terrestrialTime = fromJulianDay(parameters.meanJulianEphemerisDay + correction + planetaryCorrection)
        return terrestrialTime.minusSeconds(TerrestrialTime.getDeltaT(terrestrialTime.year).toLong())
    }

    private fun getMeanPhaseJulianEphemerisDay(k: Double, t: Double): Double {
        return 2451550.09766 + SYNODIC_MONTH_DAYS * k +
                polynomial(t, 0.0, 0.0, 0.00015437, -0.000000150, 0.00000000073)
    }

    private val CORRECTION_PLANETARY_TERMS = arrayOf(
        doubleArrayOf(0.000325, 299.77, 0.107408, -0.009173),
        doubleArrayOf(0.000165, 251.88, 0.016321, 0.0),
        doubleArrayOf(0.000164, 251.83, 26.651886, 0.0),
        doubleArrayOf(0.000126, 349.42, 36.412478, 0.0),
        doubleArrayOf(0.000110, 84.66, 18.206239, 0.0),
        doubleArrayOf(0.000062, 141.74, 53.303771, 0.0),
        doubleArrayOf(0.000060, 207.14, 2.453732, 0.0),
        doubleArrayOf(0.000056, 154.84, 7.306860, 0.0),
        doubleArrayOf(0.000047, 34.52, 27.261239, 0.0),
        doubleArrayOf(0.000042, 207.19, 0.121824, 0.0),
        doubleArrayOf(0.000040, 291.34, 1.844379, 0.0),
        doubleArrayOf(0.000037, 161.72, 24.198154, 0.0),
        doubleArrayOf(0.000035, 239.56, 25.513099, 0.0),
        doubleArrayOf(0.000023, 331.55, 3.592518, 0.0)
    )
}

internal data class MoonPhaseTimeParameters(
    val k: Double,
    val julianCenturiesSinceJ2000: Double,
    val meanJulianEphemerisDay: Double,
    val eccentricity: Double,
    val sunMeanAnomaly: Double,
    val moonMeanAnomaly: Double,
    val argumentOfLatitude: Double,
    val ascendingNode: Double
)
