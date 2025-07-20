package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.science.astronomy.OrbitalMath
import com.kylecorry.sol.science.astronomy.corrections.EclipticObliquity
import com.kylecorry.sol.science.astronomy.corrections.TerrestrialTime
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies
import com.kylecorry.sol.time.Time.plusMillis
import com.kylecorry.sol.units.Distance
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.pow

internal class Sun : ICelestialLocator {

    private val semiMajorAxisLen0 = 1.495985e8
    private val angularDiameter0 = 0.533128

    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val delta = TerrestrialTime.getDeltaT(ut.year)
        val tt = ut.plusMillis((delta * 1000).toLong())
        val T = tt.toJulianCenturies()
        val apparentLongitude = getApparentLongitude(T)
        val correctedObliquity = getObliquityCorrection(T)
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
        val T = ut.toJulianCenturies()
        val trueAnomaly = getTrueAnomaly(T)
        val ecc = getEccentricity(T)
        val f = (1 + ecc * cosDegrees(trueAnomaly)) / (1 - ecc.pow(2))
        return Distance.kilometers((semiMajorAxisLen0 / f).toFloat())
    }

    fun getAngularDiameter(ut: UniversalTime): Double {
        val au = getDistance(ut).meters().distance / 149597870700.0
        return 2 * 959.63 / (3600 * au)
    }

    fun getMeanAnomaly(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return getMeanAnomaly(T)
    }

    private fun getMeanAnomaly(T: Double): Double {
        return SolMath.normalizeAngle(SolMath.polynomial(T, 357.5291092, 35999.0502909, -0.0001536, 1 / 24490000.0))
    }

    private fun getTrueAnomaly(T: Double): Double {
        val mean = getMeanAnomaly(T)
        val center = equationOfCenter(T)
        return OrbitalMath.getTrueAnomaly(mean, center)
    }

    private fun getApparentLongitude(T: Double): Double {
        val trueLng = getTrueLongitude(T)
        val omega = SolMath.polynomial(T, 125.04, -1934.136)
        return trueLng - 0.00569 - 0.00478 * sinDegrees(omega)
    }

    private fun getTrueLongitude(T: Double): Double {
        val L = getGeometricLongitude(T)
        val C = equationOfCenter(T)
        return L + C
    }

    private fun getGeometricLongitude(T: Double): Double {
        return SolMath.normalizeAngle(SolMath.polynomial(T, 280.46646, 36000.76983, 0.0003032))
    }

    private fun getEccentricity(T: Double): Double {
        return SolMath.polynomial(T, 0.01675104, -0.0000418, -0.000000126)
    }

    private fun equationOfCenter(T: Double): Double {
        val M = getMeanAnomaly(T)
        return SolMath.polynomial(T, 1.914602, -0.004817, -0.000014) * sinDegrees(M) +
                SolMath.polynomial(T, 0.019993, -0.000101) * sinDegrees(2 * M) +
                0.000289 * sinDegrees(3 * M)
    }

    private fun getObliquityCorrection(T: Double): Double {
        val e = EclipticObliquity.getMeanObliquityOfEcliptic(T)
        val omega = SolMath.polynomial(T, 125.04, -1934.136)
        return e + 0.00256 * cosDegrees(omega)
    }

}