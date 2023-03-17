package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.corrections.EclipticObliquity
import com.kylecorry.sol.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies
import java.time.Duration
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.pow

internal class Sun : ICelestialLocator {

    private val semiMajorAxisLen0 = 1.495985e8
    private val angularDiameter0 = 0.533128

    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val delta = TerrestrialTime.getDeltaT(ut.year)
        val tt = ut.plus(Duration.ofMillis((delta * 1000).toLong()))
        val apparentLongitude = getApparentLongitude(tt)
        val correctedObliquity = getObliquityCorrection(tt)
        val rightAscension = SolMath.normalizeAngle(
            atan2(
                cosDegrees(correctedObliquity) * sinDegrees(apparentLongitude),
                cosDegrees(apparentLongitude)
            ).toDegrees()
        )
        val declination = wrap(
            asin(sinDegrees(correctedObliquity) * sinDegrees(apparentLongitude)).toDegrees(),
            -90.0, 90.0
        )
        return EquatorialCoordinate(declination, rightAscension, true)
    }

    override fun getDistance(ut: UniversalTime): Distance {
        val trueAnomaly = getTrueAnomaly(ut)
        val f =
            (1 + getEccentricity(ut) * cosDegrees(trueAnomaly)) / (1 - getEccentricity(ut).pow(2))
        return Distance.kilometers((semiMajorAxisLen0 / f).toFloat())
    }

    fun getAngularDiameter(ut: UniversalTime): Double {
        val au = getDistance(ut).meters().distance / 149597870700.0
        return 2 * 959.63 / (3600 * au)
    }

    fun getMeanAnomaly(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return SolMath.normalizeAngle(SolMath.polynomial(T, 357.5291092, 35999.0502909, -0.0001536, 1 / 24490000.0))
    }

    fun getTrueAnomaly(ut: UniversalTime): Double {
        val mean = getMeanAnomaly(ut)
        val center = equationOfCenter(ut)
        return mean + center
    }

    private fun getApparentLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val trueLng = getTrueLongitude(ut)
        val omega = SolMath.polynomial(T, 125.04, -1934.136)
        return trueLng - 0.00569 - 0.00478 * sinDegrees(omega)
    }

    private fun getTrueLongitude(ut: UniversalTime): Double {
        val L = getGeometricLongitude(ut)
        val C = equationOfCenter(ut)
        return L + C
    }

    private fun getGeometricLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return SolMath.normalizeAngle(SolMath.polynomial(T, 280.46646, 36000.76983, 0.0003032))
    }

    private fun getEccentricity(ut: UniversalTime): Double {
        val t = ut.toJulianCenturies()
        return SolMath.polynomial(t, 0.01675104, -0.0000418, -0.000000126)
    }

    private fun equationOfCenter(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val M = getMeanAnomaly(ut)
        return SolMath.polynomial(T, 1.914602, -0.004817, -0.000014) * sinDegrees(M) +
                SolMath.polynomial(T, 0.019993, -0.000101) * sinDegrees(2 * M) +
                0.000289 * sinDegrees(3 * M)
    }

    private fun getObliquityCorrection(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val e = EclipticObliquity.getMeanObliquityOfEcliptic(ut)
        val omega = SolMath.polynomial(T, 125.04, -1934.136)
        return e + 0.00256 * cosDegrees(omega)
    }

}