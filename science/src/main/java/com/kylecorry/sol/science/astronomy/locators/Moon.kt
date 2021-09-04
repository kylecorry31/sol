package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.TSMath.cosDegrees
import com.kylecorry.sol.math.TSMath.sinDegrees
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.corrections.EclipticObliquity
import com.kylecorry.sol.science.astronomy.corrections.LongitudinalNutation
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.units.EclipticCoordinate
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies
import kotlin.math.absoluteValue
import kotlin.math.floor

internal class Moon : ICelestialLocator {

    private val sun = Sun()

    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val T = ut.toJulianCenturies()
        val L = com.kylecorry.sol.math.TSMath.reduceAngleDegrees(
            com.kylecorry.sol.math.TSMath.polynomial(
                T,
                218.3164477,
                481267.88123421,
                -0.0015786,
                1 / 538841.0,
                -1 / 65194000.0
            )
        )

        val D = getMeanElongation(ut)

        val M = sun.getMeanAnomaly(ut)
        val Mprime = getMeanAnomaly(ut)

        val F = getArgumentOfLatitude(ut)

        val a1 = com.kylecorry.sol.math.TSMath.reduceAngleDegrees(119.75 + 131.849 * T)
        val a2 = com.kylecorry.sol.math.TSMath.reduceAngleDegrees(53.09 + 479264.290 * T)
        val a3 = com.kylecorry.sol.math.TSMath.reduceAngleDegrees(313.45 + 481266.484 * T)
        val E = com.kylecorry.sol.math.TSMath.polynomial(T, 1.0, -0.002516, -0.0000075)
        val E2 = com.kylecorry.sol.math.TSMath.square(E)

        val t47a = table47a()
        val t47b = table47b()

        var sumL = 0.0
        var sumB = 0.0

        for (row in t47a) {
            val eTerm = when (row[1].absoluteValue) {
                1 -> E
                2 -> E2
                else -> 1.0
            }
            sumL += row[4] * eTerm * sinDegrees(row[0] * D + row[1] * M + row[2] * Mprime + row[3] * F)
        }

        for (row in t47b) {
            val eTerm = when (row[1].absoluteValue) {
                1 -> E
                2 -> E2
                else -> 1.0
            }
            sumB += row[4] * eTerm * sinDegrees(row[0] * D + row[1] * M + row[2] * Mprime + row[3] * F)
        }

        sumL += 3958 * sinDegrees(a1) + 1962 * sinDegrees(L - F) + 318 * sinDegrees(a2)
        sumB += -2235 * sinDegrees(L) + 382 * sinDegrees(a3) + 175 * sinDegrees(a1 - F) +
                175 * sinDegrees(a1 + F) + 127 * sinDegrees(L - Mprime) - 115 * sinDegrees(L + Mprime)


        val apparentLongitude =
            L + sumL / 1000000.0 + LongitudinalNutation.getNutationInLongitude(ut)
        val eclipticLatitude = sumB / 1000000.0
        val eclipticObliquity = EclipticObliquity.getTrueObliquityOfEcliptic(ut)

        return EclipticCoordinate(eclipticLatitude, apparentLongitude).toEquatorial(
            eclipticObliquity
        )
    }

    fun getDistance(ut: UniversalTime): Distance {
        val T = ut.toJulianCenturies()
        val D = getMeanElongation(ut)
        val F = getArgumentOfLatitude(ut)
        val M = sun.getMeanAnomaly(ut)

        val Mprime = getMeanAnomaly(ut)
        val E = com.kylecorry.sol.math.TSMath.polynomial(T, 1.0, -0.002516, -0.0000075)
        val E2 = com.kylecorry.sol.math.TSMath.square(E)
        val t47a = table47a()
        var sumR = 0.0

        for (row in t47a) {
            val eTerm = when (row[1].absoluteValue) {
                1 -> E
                2 -> E2
                else -> 1.0
            }
            sumR += row[5] * eTerm * cosDegrees(row[0] * D + row[1] * M + row[2] * Mprime + row[3] * F)
        }
        val distanceKm = 385000.56 + sumR / 1000
        return Distance.kilometers(distanceKm.toFloat())
    }

    fun getAngularDiameter(ut: UniversalTime, location: Coordinate = Coordinate.zero): Double {
        val distance = getDistance(ut).convertTo(DistanceUnits.Kilometers).distance
        val s = 358743400 / distance
        val sinPi = 6378.14 / distance
        val h = AstroUtils.getAltitude(this, ut, location, true).toDouble()
        return (s * (1 + sinDegrees(h) * sinPi)) * 0.000277778 * 2
    }

    fun getMeanAnomaly(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return com.kylecorry.sol.math.TSMath.reduceAngleDegrees(
            com.kylecorry.sol.math.TSMath.polynomial(
                T,
                134.9633964,
                477198.8675055,
                0.0087414,
                1 / 69699.0,
                -1 / 14712000.0
            )
        )
    }

    fun getPhase(ut: UniversalTime): MoonPhase {
        val phaseAngle = getMoonPhaseAngle(ut)
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

    fun getNextPhaseK(date: UniversalTime, moonTruePhase: MoonTruePhase): Double {
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

    private fun getMoonPhaseAngle(ut: UniversalTime): Double {
        val D = getMeanElongation(ut)
        val M = sun.getMeanAnomaly(ut)
        val Mp = getMeanAnomaly(ut)

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

    private fun getMoonIllumination(phaseAngle: Double): Double {
        return ((1 + cosDegrees(phaseAngle - 180)) / 2) * 100
    }

    private fun getMeanElongation(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return com.kylecorry.sol.math.TSMath.reduceAngleDegrees(
            com.kylecorry.sol.math.TSMath.polynomial(
                T,
                297.8501921,
                445267.1114034,
                -0.0018819,
                1 / 545868.0,
                -1 / 113065000.0
            )
        )
    }

    private fun getArgumentOfLatitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return com.kylecorry.sol.math.TSMath.reduceAngleDegrees(
            com.kylecorry.sol.math.TSMath.polynomial(
                T,
                93.2720950,
                483202.0175233,
                -0.0036539,
                -1 / 3526000.0,
                1 / 863310000.0
            )
        )
    }

    private fun table47a(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(0, 0, 1, 0, 6288774, -20905355),
            arrayOf(2, 0, -1, 0, 1274027, -3699111),
            arrayOf(2, 0, 0, 0, 658314, -2955968),
            arrayOf(0, 0, 2, 0, 213618, -569925),
            arrayOf(0, 1, 0, 0, -185116, 48888),
            arrayOf(0, 0, 0, 2, -114332, -3149),
            arrayOf(2, 0, -2, 0, 58793, 246158),
            arrayOf(2, -1, -1, 0, 57066, -152138),
            arrayOf(2, 0, 1, 0, 53322, -170733),
            arrayOf(2, -1, 0, 0, 45758, -204586),
            arrayOf(0, 1, -1, 0, -40923, -129620),
            arrayOf(1, 0, 0, 0, -34720, 108743),
            arrayOf(0, 1, 1, 0, -30383, 104755),
            arrayOf(2, 0, 0, -2, 15327, 10321),
            arrayOf(0, 0, 1, 2, -12528, 0),
            arrayOf(0, 0, 1, -2, 10980, 79661),
            arrayOf(4, 0, -1, 0, 10675, -34782),
            arrayOf(0, 0, 3, 0, 10034, -23210),
            arrayOf(4, 0, -2, 0, 8548, -21636),
            arrayOf(2, 1, -1, 0, -7888, 24208),
            arrayOf(2, 1, 0, 0, -6766, 30824),
            arrayOf(1, 0, -1, 0, -5163, -8379),
            arrayOf(1, 1, 0, 0, 4987, -16675),
            arrayOf(2, -1, 1, 0, 4036, -12831),
            arrayOf(2, 0, 2, 0, 3994, -10445),
            arrayOf(4, 0, 0, 0, 3861, -11650),
            arrayOf(2, 0, -3, 0, 3665, 14403),
            arrayOf(0, 1, -2, 0, -2689, -7003),
            arrayOf(2, 0, -1, 2, -2602, 0),
            arrayOf(2, -1, -2, 0, 2390, 10056),
            arrayOf(1, 0, 1, 0, -2348, 6322),
            arrayOf(2, -2, 0, 0, 2236, -9884),
            arrayOf(0, 1, 2, 0, -2120, 5751),
            arrayOf(0, 2, 0, 0, -2069, 0),
            arrayOf(2, -2, -1, 0, 2048, -4950),
            arrayOf(2, 0, 1, -2, -1773, 4130),
            arrayOf(2, 0, 0, 2, -1595, 0),
            arrayOf(4, -1, -1, 0, 1215, -3958),
            arrayOf(0, 0, 2, 2, -1110, 0),
            arrayOf(3, 0, -1, 0, -892, 3258),
            arrayOf(2, 1, 1, 0, -810, 2616),
            arrayOf(4, -1, -2, 0, 759, -1897),
            arrayOf(0, 2, -1, 0, -713, -2117),
            arrayOf(2, 2, -1, 0, -700, 2354),
            arrayOf(2, 1, -2, 0, 691, 0),
            arrayOf(2, -1, 0, -2, 596, 0),
            arrayOf(4, 0, 1, 0, 549, -1423),
            arrayOf(0, 0, 4, 0, 537, -1117),
            arrayOf(4, -1, 0, 0, 520, -1571),
            arrayOf(1, 0, -2, 0, -487, -1739),
            arrayOf(2, 1, 0, -2, -399, 0),
            arrayOf(0, 0, 2, -2, -381, -4421),
            arrayOf(1, 1, 1, 0, 351, 0),
            arrayOf(3, 0, -2, 0, -340, 0),
            arrayOf(4, 0, -3, 0, 330, 0),
            arrayOf(2, -1, 2, 0, 327, 0),
            arrayOf(0, 2, 1, 0, -323, 1165),
            arrayOf(1, 1, -1, 0, 299, 0),
            arrayOf(2, 0, 3, 0, 294, 0),
            arrayOf(2, 0, -1, -2, 0, 8752)
        )
    }

    private fun table47b(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(0, 0, 0, 1, 5128122),
            arrayOf(0, 0, 1, 1, 280602),
            arrayOf(0, 0, 1, -1, 277693),
            arrayOf(2, 0, 0, -1, 173237),
            arrayOf(2, 0, -1, 1, 55413),
            arrayOf(2, 0, -1, -1, 46271),
            arrayOf(2, 0, 0, 1, 32573),
            arrayOf(0, 0, 2, 1, 17198),
            arrayOf(2, 0, 1, -1, 9266),
            arrayOf(0, 0, 2, -1, 8822),
            arrayOf(2, -1, 0, -1, 8216),
            arrayOf(2, 0, -2, -1, 4324),
            arrayOf(2, 0, 1, 1, 4200),
            arrayOf(2, 1, 0, -1, -3359),
            arrayOf(2, -1, -1, 1, 2463),
            arrayOf(2, -1, 0, 1, 2211),
            arrayOf(2, -1, -1, -1, 2065),
            arrayOf(0, 1, -1, -1, -1870),
            arrayOf(4, 0, -1, -1, 1828),
            arrayOf(0, 1, 0, 1, -1794),
            arrayOf(0, 0, 0, 3, -1749),
            arrayOf(0, 1, -1, 1, -1565),
            arrayOf(1, 0, 0, 1, -1491),
            arrayOf(0, 1, 1, 1, -1475),
            arrayOf(0, 1, 1, -1, -1410),
            arrayOf(0, 1, 0, -1, -1344),
            arrayOf(1, 0, 0, -1, -1335),
            arrayOf(0, 0, 3, 1, 1107),
            arrayOf(4, 0, 0, -1, 1021),
            arrayOf(4, 0, -1, 1, 833),
            arrayOf(0, 0, 1, -3, 777),
            arrayOf(4, 0, -2, 1, 671),
            arrayOf(2, 0, 0, -3, 607),
            arrayOf(2, 0, 2, -1, 596),
            arrayOf(2, -1, 1, -1, 491),
            arrayOf(2, 0, -2, 1, -451),
            arrayOf(0, 0, 3, -1, 439),
            arrayOf(2, 0, 2, 1, 422),
            arrayOf(2, 0, -3, -1, 421),
            arrayOf(2, 1, -1, 1, -366),
            arrayOf(2, 1, 0, 1, -351),
            arrayOf(4, 0, 0, 1, 331),
            arrayOf(2, -1, 1, 1, 315),
            arrayOf(2, -2, 0, -1, 302),
            arrayOf(0, 0, 1, 3, -283),
            arrayOf(2, 1, 1, -1, -229),
            arrayOf(1, 1, 0, -1, 223),
            arrayOf(1, 1, 0, 1, 223),
            arrayOf(0, 1, -2, -1, -220),
            arrayOf(2, 1, -1, -1, -220),
            arrayOf(1, 0, 1, 1, -185),
            arrayOf(2, -1, -2, -1, 181),
            arrayOf(0, 1, 2, 1, -177),
            arrayOf(4, 0, -2, -1, 176),
            arrayOf(4, -1, -1, -1, 166),
            arrayOf(1, 0, 1, -1, -164),
            arrayOf(4, 0, 1, -1, 132),
            arrayOf(1, 0, -1, -1, -119),
            arrayOf(4, -1, 0, -1, 115),
            arrayOf(2, -2, 0, 1, 107)
        )
    }

}