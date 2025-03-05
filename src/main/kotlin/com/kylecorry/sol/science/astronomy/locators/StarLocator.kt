package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.science.astronomy.stars.Star
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianCenturies
import com.kylecorry.sol.units.Distance
import kotlin.math.*

internal class StarLocator(private val star: Star) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val t = ut.toJulianCenturies()

        val coords = star.coordinate
        val motion = star.motion

        val value1 = SolMath.polynomial(
            t,
            0.0,
            2306.2181,
            0.30188,
            0.017998
        ) / 3600

        val value2 = SolMath.polynomial(
            t,
            0.0,
            2306.2181,
            1.09468,
            0.018203
        ) / 3600

        val value3 = SolMath.polynomial(
            t,
            0.0,
            2004.3109,
            -0.42665,
            -0.041833
        ) / 3600

        val processionRA =
            motion.rightAscension * t * 100
        val processionDec = motion.declination * t * 100

        val declination = coords.declination + processionDec
        val rightAscension = coords.rightAscension + processionRA

        val A = cosDegrees(declination) * sinDegrees(rightAscension + value1)
        val B =
            cosDegrees(value3) * cosDegrees(declination) * cosDegrees(rightAscension + value1) - sinDegrees(value3) * sinDegrees(
                declination
            )
        val C =
            sinDegrees(value3) * cosDegrees(declination) * cosDegrees(rightAscension + value1) + cosDegrees(value3) * sinDegrees(
                declination
            )

        val ra = atan2(A, B).toDegrees() + value2
        val dec = if (declination.absoluteValue > 89.0) {
            acos(hypot(A, B))
        } else {
            asin(C)
        }.toDegrees()

        return EquatorialCoordinate(dec, ra)
    }

    override fun getDistance(ut: UniversalTime): Distance? {
        return null
    }
}