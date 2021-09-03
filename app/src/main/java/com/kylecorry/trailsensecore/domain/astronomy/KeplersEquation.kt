package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.math.*
import kotlin.math.*

object KeplersEquation {

    fun getEccentricAnomaly(meanAnomaly: Double, orbitalEccentricity: Double): Double {
        val mr = meanAnomaly.toRadians()
        var er = mr

        val iterations = 100
        val threshold = 0.000002
        for (i in 0 until iterations) {
            val prev = er
            er = mr + orbitalEccentricity * sin(er)

            if ((er - prev).absoluteValue <= threshold) {
                break
            }
        }

        return er.toDegrees()
    }

    fun getTrueAnomaly(meanAnomaly: Double, orbitalEccentricity: Double): Double {
        val eccentricAnomaly = getEccentricAnomaly(meanAnomaly, orbitalEccentricity)
        val t1 =
            (cosDegrees(eccentricAnomaly) - orbitalEccentricity) / (1 - orbitalEccentricity * cosDegrees(
                eccentricAnomaly
            ))
        val v = acos(t1).toDegrees()
        return wrap(v, 0.0, 360.0)
    }

}