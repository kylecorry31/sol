package com.kylecorry.trailsensecore.astronomy.units

import com.kylecorry.andromeda.core.math.*
import com.kylecorry.trailsensecore.domain.math.MathUtils
import kotlin.math.asin
import kotlin.math.atan2

class EclipticCoordinate(_eclipticLatitude: Double, _eclipticLongitude: Double) {
    val eclipticLatitude = wrap(_eclipticLatitude, -90.0, 90.0)
    val eclipticLongitude = wrap(_eclipticLongitude, 0.0, 360.0)

    fun toEquatorial(eclipticObliquity: Double): EquatorialCoordinate {
        val e = eclipticObliquity
        val t =
            sinDegrees(eclipticLatitude) * cosDegrees(e) + cosDegrees(eclipticLatitude) * sinDegrees(
                e
            ) * sinDegrees(eclipticLongitude)

        val declination = asin(t).toDegrees()

        val y =
            sinDegrees(eclipticLongitude) * cosDegrees(e) - tanDegrees(eclipticLatitude) * sinDegrees(
                e
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
            return e0 - MathUtils.polynomial(t, 0.0, 46.815, 0.0006, -0.00181) / 3600
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
                val omega = MathUtils.polynomial(ut.toJulianCenturies(), 125.04, -1934.136)
                lon += 0.00569 + 0.00478 * sinDegrees(omega)
            }

            return EclipticCoordinate(lat, lon)
        }

    }


}