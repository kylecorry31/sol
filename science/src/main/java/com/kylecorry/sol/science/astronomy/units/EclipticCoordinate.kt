package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.tanDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.wrap
import kotlin.math.asin
import kotlin.math.atan2

internal class EclipticCoordinate(_eclipticLatitude: Double, _eclipticLongitude: Double) {
    val eclipticLatitude = wrap(_eclipticLatitude, -90.0, 90.0)
    val eclipticLongitude = wrap(_eclipticLongitude, 0.0, 360.0)

    fun toEquatorial(eclipticObliquity: Double): EquatorialCoordinate {
        val t =
            sinDegrees(eclipticLatitude) * cosDegrees(eclipticObliquity) + cosDegrees(eclipticLatitude) * sinDegrees(
                eclipticObliquity
            ) * sinDegrees(eclipticLongitude)

        val declination = asin(t).toDegrees()

        val y =
            sinDegrees(eclipticLongitude) * cosDegrees(eclipticObliquity) - tanDegrees(eclipticLatitude) * sinDegrees(
                eclipticObliquity
            )

        val x = cosDegrees(eclipticLongitude)

        val rightAscension = atan2(y, x).toDegrees()

        return EquatorialCoordinate(declination, rightAscension)
    }

    fun toEquatorial(ut: UniversalTime): EquatorialCoordinate {
        return toEquatorial(getObliquityOfTheEcliptic(ut))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EclipticCoordinate

        if (eclipticLatitude != other.eclipticLatitude) return false
        if (eclipticLongitude != other.eclipticLongitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eclipticLatitude.hashCode()
        result = 31 * result + eclipticLongitude.hashCode()
        return result
    }


    companion object {
        fun getObliquityOfTheEcliptic(ut: UniversalTime): Double {
            val e0 = 23.439292
            val t = ut.toJulianCenturies()
            return e0 - SolMath.polynomial(t, 0.0, 46.815, 0.0006, -0.00181) / 3600
        }

        fun fromEquatorial(
            equatorial: EquatorialCoordinate,
            ut: UniversalTime
        ): EclipticCoordinate {
            val e = getObliquityOfTheEcliptic(ut)
            val alpha = equatorial.rightAscension
            val delta = equatorial.declination
            val t0 = sinDegrees(delta) * cosDegrees(e)
            val t1 = cosDegrees(delta) * sinDegrees(e) * sinDegrees(alpha)

            val t = t0 - t1
            val lat = asin(t).toDegrees()
            val y =
                sinDegrees(alpha) * cosDegrees(e) + tanDegrees(delta) * sinDegrees(
                    e
                )

            val x = cosDegrees(alpha)

            var lon = atan2(y, x).toDegrees()

            if (equatorial.isApparent) {
                val omega = SolMath.polynomial(ut.toJulianCenturies(), 125.04, -1934.136)
                lon += 0.00569 + 0.00478 * sinDegrees(omega)
            }

            return EclipticCoordinate(lat, lon)
        }

    }


}