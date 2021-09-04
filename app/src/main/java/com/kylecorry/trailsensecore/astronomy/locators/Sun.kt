package com.kylecorry.trailsensecore.astronomy.locators

import com.kylecorry.andromeda.core.math.cosDegrees
import com.kylecorry.andromeda.core.math.sinDegrees
import com.kylecorry.andromeda.core.math.toDegrees
import com.kylecorry.andromeda.core.math.wrap
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.astronomy.corrections.EclipticObliquity
import com.kylecorry.trailsensecore.astronomy.units.UniversalTime
import com.kylecorry.trailsensecore.astronomy.units.toJulianCenturies
import com.kylecorry.trailsensecore.domain.math.MathUtils
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.pow

class Sun : ICelestialLocator {

    private val semiMajorAxisLen0 = 1.495985e8
    private val angularDiameter0 = 0.533128

    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val apparentLongitude = getApparentLongitude(ut)
        val correctedObliquity = getObliquityCorrection(ut)
        val rightAscension = MathUtils.reduceAngleDegrees(
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

    fun getDistance(ut: UniversalTime): Distance {
        val trueAnomaly = getTrueAnomaly(ut)
        val f =
            (1 + getEccentricity(ut) * cosDegrees(trueAnomaly)) / (1 - getEccentricity(ut).pow(2))
        return Distance.kilometers((semiMajorAxisLen0 / f).toFloat())
    }

    fun getAngularDiameter(ut: UniversalTime): Double {
        val trueAnomaly = getTrueAnomaly(ut)
        val f =
            (1 + getEccentricity(ut) * cosDegrees(trueAnomaly)) / (1 - getEccentricity(ut).pow(2))
        return angularDiameter0 * f
    }

    fun getMeanAnomaly(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return MathUtils.reduceAngleDegrees(MathUtils.polynomial(T, 357.52911, 35999.05029, -0.0001537))//, 1 / 24490000.0))
    }

    fun getTrueAnomaly(ut: UniversalTime): Double {
        val mean = getMeanAnomaly(ut)
        val center = equationOfCenter(ut)
        return mean + center
    }

    private fun getApparentLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val trueLng = getTrueLongitude(ut)
        val omega = MathUtils.polynomial(T, 125.04, -1934.136)
        return trueLng - 0.00569 - 0.00478 * sinDegrees(omega)
    }

    private fun getTrueLongitude(ut: UniversalTime): Double {
        val L = getGeometricLongitude(ut)
        val C = equationOfCenter(ut)
        return L + C
    }

    private fun getGeometricLongitude(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        return MathUtils.reduceAngleDegrees(MathUtils.polynomial(T, 280.46646, 36000.76983, 0.0003032))
    }

    private fun getEccentricity(ut: UniversalTime): Double {
        val t = ut.toJulianCenturies()
        return MathUtils.polynomial(t, 0.01675104, -0.0000418, -0.000000126)
    }

    private fun equationOfCenter(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val M = getMeanAnomaly(ut)
        return MathUtils.polynomial(T, 1.914602, -0.004817, -0.000014) * sinDegrees(M) +
                MathUtils.polynomial(T, 0.019993, -0.000101) * sinDegrees(2 * M) +
                0.000289 * sinDegrees(3 * M)
    }

    private fun getObliquityCorrection(ut: UniversalTime): Double {
        val T = ut.toJulianCenturies()
        val e = EclipticObliquity.getMeanObliquityOfEcliptic(ut)
        val omega = MathUtils.polynomial(T, 125.04, -1934.136)
        return e + 0.00256 * cosDegrees(omega)
    }

}