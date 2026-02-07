package com.kylecorry.sol.math

import com.kylecorry.sol.math.SolMath.real
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * A spherical coordinate
 * @param r the radius
 * @param theta the angle from the z-axis (in degrees)
 * @param phi the angle from the x-axis (in degrees)
 */
data class SphericalCoordinate(
    val r: Float,
    val theta: Float,
    val phi: Float
) {
    fun toCartesian(): Vector3 {
        val thetaRad = theta.toRadians()
        val phiRad = phi.toRadians()

        val sinTheta = sin(thetaRad)
        val x = r * sinTheta * cos(phiRad)
        val y = r * sinTheta * sin(phiRad)
        val z = r * cos(thetaRad)
        return Vector3(x, y, z)
    }

    companion object {
        fun fromCartesian(cartesian: Vector3): SphericalCoordinate {
            val r = cartesian.magnitude()
            val theta = acos(cartesian.z / r).toDegrees().real(0f)
            val phi = atan2(cartesian.y, cartesian.x).toDegrees().real(0f)
            return SphericalCoordinate(r, theta, phi)
        }
    }

}
