package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.square
import kotlin.math.sqrt

data class ReferenceEllipsoid(val semiMajorAxis: Double, val semiMinorAxis: Double) {

    val a = semiMajorAxis
    val b = semiMinorAxis
    val flattening = (a - b) / a
    val inverseFlattening = 1 / flattening
    val squaredEccentricity = (square(a) - square(b)) / (square(a))
    val eccentricity = sqrt(squaredEccentricity)

    companion object {

        val wgs84 = ReferenceEllipsoid(6378137.0, 6356752.3142)
        val sphere = ReferenceEllipsoid(6371000.0, 6371000.0)

        fun new(semiMajorAxis: Double, flattening: Double): ReferenceEllipsoid {
            val semiMinorAxis = -(flattening * semiMajorAxis - semiMajorAxis)
            return ReferenceEllipsoid(semiMajorAxis, semiMinorAxis)
        }
    }

}