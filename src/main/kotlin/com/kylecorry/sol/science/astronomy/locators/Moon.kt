package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.MathExtensions.toRadians
import com.kylecorry.sol.math.algebra.Algebra.polynomial
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.trigonometry.Trigonometry
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import com.kylecorry.sol.science.astronomy.AstroUtils
import com.kylecorry.sol.science.astronomy.corrections.EclipticObliquity
import com.kylecorry.sol.science.astronomy.corrections.LongitudinalNutation
import com.kylecorry.sol.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.sol.science.astronomy.moon.MoonPhase
import com.kylecorry.sol.science.astronomy.moon.MoonTruePhase
import com.kylecorry.sol.science.astronomy.units.EclipticCoordinate
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.fromJulianDay
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import java.time.Duration
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

internal class Moon : ICelestialLocator {

    private val sun = Sun()

    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val delta = TerrestrialTime.getDeltaT(ut.year)
        val tt = ut.plus(Duration.ofMillis((delta * 1000).toLong()))
        val julianCenturiesSinceJ2000 = tt.toJulianCenturies()
        val L = Trigonometry.normalizeAngle(
            polynomial(
                julianCenturiesSinceJ2000,
                218.3164477,
                481267.88123421,
                -0.0015786,
                1 / 538841.0,
                -1 / 65194000.0
            )
        )

        val D = getMeanElongation(tt)

        val M = sun.getMeanAnomaly(tt)
        val Mprime = getMeanAnomaly(tt)

        val F = getArgumentOfLatitude(tt)

        val a1 = Trigonometry.normalizeAngle(119.75 + 131.849 * julianCenturiesSinceJ2000)
        val a2 = Trigonometry.normalizeAngle(53.09 + 479264.29 * julianCenturiesSinceJ2000)
        val a3 = Trigonometry.normalizeAngle(313.45 + 481266.484 * julianCenturiesSinceJ2000)
        val E = polynomial(julianCenturiesSinceJ2000, 1.0, -0.002516, -0.0000074)
        val E2 = Arithmetic.square(E)

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
            L + sumL / 1000000.0 + LongitudinalNutation.getNutationInLongitude(tt)
        val eclipticLatitude = sumB / 1000000.0
        val eclipticObliquity = EclipticObliquity.getTrueObliquityOfEcliptic(tt)

        return EclipticCoordinate(eclipticLatitude, apparentLongitude).toEquatorial(
            eclipticObliquity
        )
    }

    override fun getDistance(ut: UniversalTime): Distance {
        val delta = TerrestrialTime.getDeltaT(ut.year)
        val tt = ut.plus(Duration.ofMillis((delta * 1000).toLong()))
        val julianCenturiesSinceJ2000 = tt.toJulianCenturies()
        val D = getMeanElongation(tt)
        val F = getArgumentOfLatitude(tt)
        val M = sun.getMeanAnomaly(tt)

        val Mprime = getMeanAnomaly(tt)
        val E = polynomial(julianCenturiesSinceJ2000, 1.0, -0.002516, -0.0000075)
        val E2 = Arithmetic.square(E)
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
        val distance = getDistance(ut).convertTo(DistanceUnits.Kilometers).value
        val s = 358743400 / distance
        val sinPi = 6378.14 / distance
        val h = AstroUtils.getAltitude(this, ut, location, true).toDouble()
        return (s * (1 + sinDegrees(h) * sinPi)) * 0.000277778 * 2
    }

    fun getMeanAnomaly(ut: UniversalTime): Double {
        val julianCenturiesSinceJ2000 = ut.toJulianCenturies()
        return Trigonometry.normalizeAngle(
            polynomial(
                julianCenturiesSinceJ2000,
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

        for (phase in MoonTruePhase.entries) {
            if (phase.startAngle <= phaseAngle && phase.endAngle >= phaseAngle) {
                return MoonPhase(phase, illumination, phaseAngle.toFloat())
            }

            // Handle new moon
            if (phase.startAngle >= phase.endAngle) {
                if (phase.startAngle <= phaseAngle || phase.endAngle >= phaseAngle) {
                    return MoonPhase(phase, illumination, phaseAngle.toFloat())
                }
            }
        }

        return MoonPhase(MoonTruePhase.New, illumination, phaseAngle.toFloat())
    }

    fun getNextMeanPhase(date: UniversalTime, moonTruePhase: MoonTruePhase): UniversalTime {
        val k = getNextPhaseK(date, moonTruePhase)
        val t = k / 1236.85
        val jde = 2451550.09766 + 29.530588861 * k + polynomial(t, 0.0, 0.0, 0.00015437, -0.000000150, 0.00000000073)
        return fromJulianDay(jde)
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

    fun getTilt(date: UniversalTime, location: Coordinate): Float {
        val parallacticAngle = AstroUtils.getParallacticAngle(this, date, location)
        val moonPositionAngle = getMoonPositionAngle(date)
        return (moonPositionAngle - parallacticAngle).toFloat()
    }

    private fun getMoonPositionAngle(ut: UniversalTime): Double {
        val sunCoords = sun.getCoordinates(ut)
        val moonCoords = getCoordinates(ut)
        val sunRA = sunCoords.rightAscension.toRadians()
        val sunDec = sunCoords.declination.toRadians()
        val moonRA = moonCoords.rightAscension.toRadians()
        val moonDec = moonCoords.declination.toRadians()

        return atan2(
            cos(sunDec) * sin(sunRA - moonRA),
            sin(sunDec) * cos(moonDec) - cos(sunDec) * sin(moonDec) * cos(sunRA - moonRA)
        ).toDegrees()
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
        val julianCenturiesSinceJ2000 = ut.toJulianCenturies()
        return Trigonometry.normalizeAngle(
            polynomial(
                julianCenturiesSinceJ2000,
                297.8501921,
                445267.1114034,
                -0.0018819,
                1 / 545868.0,
                -1 / 113065000.0
            )
        )
    }

    private fun getArgumentOfLatitude(ut: UniversalTime): Double {
        val julianCenturiesSinceJ2000 = ut.toJulianCenturies()
        return Trigonometry.normalizeAngle(
            polynomial(
                julianCenturiesSinceJ2000,
                93.2720950,
                483202.0175233,
                -0.0036539,
                -1 / 3526000.0,
                1 / 863310000.0
            )
        )
    }

    private fun table47a(): Array<IntArray> {
        return arrayOf(
            intArrayOf(0, 0, 1, 0, 6288774, -20905355),
            intArrayOf(2, 0, -1, 0, 1274027, -3699111),
            intArrayOf(2, 0, 0, 0, 658314, -2955968),
            intArrayOf(0, 0, 2, 0, 213618, -569925),
            intArrayOf(0, 1, 0, 0, -185116, 48888),
            intArrayOf(0, 0, 0, 2, -114332, -3149),
            intArrayOf(2, 0, -2, 0, 58793, 246158),
            intArrayOf(2, -1, -1, 0, 57066, -152138),
            intArrayOf(2, 0, 1, 0, 53322, -170733),
            intArrayOf(2, -1, 0, 0, 45758, -204586),
            intArrayOf(0, 1, -1, 0, -40923, -129620),
            intArrayOf(1, 0, 0, 0, -34720, 108743),
            intArrayOf(0, 1, 1, 0, -30383, 104755),
            intArrayOf(2, 0, 0, -2, 15327, 10321),
            intArrayOf(0, 0, 1, 2, -12528, 0),
            intArrayOf(0, 0, 1, -2, 10980, 79661),
            intArrayOf(4, 0, -1, 0, 10675, -34782),
            intArrayOf(0, 0, 3, 0, 10034, -23210),
            intArrayOf(4, 0, -2, 0, 8548, -21636),
            intArrayOf(2, 1, -1, 0, -7888, 24208),
            intArrayOf(2, 1, 0, 0, -6766, 30824),
            intArrayOf(1, 0, -1, 0, -5163, -8379),
            intArrayOf(1, 1, 0, 0, 4987, -16675),
            intArrayOf(2, -1, 1, 0, 4036, -12831),
            intArrayOf(2, 0, 2, 0, 3994, -10445),
            intArrayOf(4, 0, 0, 0, 3861, -11650),
            intArrayOf(2, 0, -3, 0, 3665, 14403),
            intArrayOf(0, 1, -2, 0, -2689, -7003),
            intArrayOf(2, 0, -1, 2, -2602, 0),
            intArrayOf(2, -1, -2, 0, 2390, 10056),
            intArrayOf(1, 0, 1, 0, -2348, 6322),
            intArrayOf(2, -2, 0, 0, 2236, -9884),
            intArrayOf(0, 1, 2, 0, -2120, 5751),
            intArrayOf(0, 2, 0, 0, -2069, 0),
            intArrayOf(2, -2, -1, 0, 2048, -4950),
            intArrayOf(2, 0, 1, -2, -1773, 4130),
            intArrayOf(2, 0, 0, 2, -1595, 0),
            intArrayOf(4, -1, -1, 0, 1215, -3958),
            intArrayOf(0, 0, 2, 2, -1110, 0),
            intArrayOf(3, 0, -1, 0, -892, 3258),
            intArrayOf(2, 1, 1, 0, -810, 2616),
            intArrayOf(4, -1, -2, 0, 759, -1897),
            intArrayOf(0, 2, -1, 0, -713, -2117),
            intArrayOf(2, 2, -1, 0, -700, 2354),
            intArrayOf(2, 1, -2, 0, 691, 0),
            intArrayOf(2, -1, 0, -2, 596, 0),
            intArrayOf(4, 0, 1, 0, 549, -1423),
            intArrayOf(0, 0, 4, 0, 537, -1117),
            intArrayOf(4, -1, 0, 0, 520, -1571),
            intArrayOf(1, 0, -2, 0, -487, -1739),
            intArrayOf(2, 1, 0, -2, -399, 0),
            intArrayOf(0, 0, 2, -2, -381, -4421),
            intArrayOf(1, 1, 1, 0, 351, 0),
            intArrayOf(3, 0, -2, 0, -340, 0),
            intArrayOf(4, 0, -3, 0, 330, 0),
            intArrayOf(2, -1, 2, 0, 327, 0),
            intArrayOf(0, 2, 1, 0, -323, 1165),
            intArrayOf(1, 1, -1, 0, 299, 0),
            intArrayOf(2, 0, 3, 0, 294, 0),
            intArrayOf(2, 0, -1, -2, 0, 8752)
        )
    }

    private fun table47b(): Array<IntArray> {
        return arrayOf(
            intArrayOf(0, 0, 0, 1, 5128122),
            intArrayOf(0, 0, 1, 1, 280602),
            intArrayOf(0, 0, 1, -1, 277693),
            intArrayOf(2, 0, 0, -1, 173237),
            intArrayOf(2, 0, -1, 1, 55413),
            intArrayOf(2, 0, -1, -1, 46271),
            intArrayOf(2, 0, 0, 1, 32573),
            intArrayOf(0, 0, 2, 1, 17198),
            intArrayOf(2, 0, 1, -1, 9266),
            intArrayOf(0, 0, 2, -1, 8822),
            intArrayOf(2, -1, 0, -1, 8216),
            intArrayOf(2, 0, -2, -1, 4324),
            intArrayOf(2, 0, 1, 1, 4200),
            intArrayOf(2, 1, 0, -1, -3359),
            intArrayOf(2, -1, -1, 1, 2463),
            intArrayOf(2, -1, 0, 1, 2211),
            intArrayOf(2, -1, -1, -1, 2065),
            intArrayOf(0, 1, -1, -1, -1870),
            intArrayOf(4, 0, -1, -1, 1828),
            intArrayOf(0, 1, 0, 1, -1794),
            intArrayOf(0, 0, 0, 3, -1749),
            intArrayOf(0, 1, -1, 1, -1565),
            intArrayOf(1, 0, 0, 1, -1491),
            intArrayOf(0, 1, 1, 1, -1475),
            intArrayOf(0, 1, 1, -1, -1410),
            intArrayOf(0, 1, 0, -1, -1344),
            intArrayOf(1, 0, 0, -1, -1335),
            intArrayOf(0, 0, 3, 1, 1107),
            intArrayOf(4, 0, 0, -1, 1021),
            intArrayOf(4, 0, -1, 1, 833),
            intArrayOf(0, 0, 1, -3, 777),
            intArrayOf(4, 0, -2, 1, 671),
            intArrayOf(2, 0, 0, -3, 607),
            intArrayOf(2, 0, 2, -1, 596),
            intArrayOf(2, -1, 1, -1, 491),
            intArrayOf(2, 0, -2, 1, -451),
            intArrayOf(0, 0, 3, -1, 439),
            intArrayOf(2, 0, 2, 1, 422),
            intArrayOf(2, 0, -3, -1, 421),
            intArrayOf(2, 1, -1, 1, -366),
            intArrayOf(2, 1, 0, 1, -351),
            intArrayOf(4, 0, 0, 1, 331),
            intArrayOf(2, -1, 1, 1, 315),
            intArrayOf(2, -2, 0, -1, 302),
            intArrayOf(0, 0, 1, 3, -283),
            intArrayOf(2, 1, 1, -1, -229),
            intArrayOf(1, 1, 0, -1, 223),
            intArrayOf(1, 1, 0, 1, 223),
            intArrayOf(0, 1, -2, -1, -220),
            intArrayOf(2, 1, -1, -1, -220),
            intArrayOf(1, 0, 1, 1, -185),
            intArrayOf(2, -1, -2, -1, 181),
            intArrayOf(0, 1, 2, 1, -177),
            intArrayOf(4, 0, -2, -1, 176),
            intArrayOf(4, -1, -1, -1, 166),
            intArrayOf(1, 0, 1, -1, -164),
            intArrayOf(4, 0, 1, -1, 132),
            intArrayOf(1, 0, -1, -1, -119),
            intArrayOf(4, -1, 0, -1, 115),
            intArrayOf(2, -2, 0, 1, 107)
        )
    }

}
