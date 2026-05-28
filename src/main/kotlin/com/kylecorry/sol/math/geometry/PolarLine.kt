package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class PolarLine(val rho: Float, val thetaRadians: Float) {
    private val cosTheta = cos(thetaRadians)
    private val sinTheta = sin(thetaRadians)

    fun evaluate(point: Vector2): Float {
        return point.x * cosTheta + point.y * sinTheta - rho
    }

    fun intersect(other: PolarLine): Vector2? {
        val determinant = cosTheta * other.sinTheta - sinTheta * other.cosTheta
        if (Arithmetic.isZero(determinant)) {
            return null
        }

        val x = (rho * other.sinTheta - sinTheta * other.rho) / determinant
        val y = (cosTheta * other.rho - rho * other.cosTheta) / determinant
        return Vector2(x, y)
    }

    fun angleBetween(other: PolarLine): Float {
        val difference = abs(thetaRadians - other.thetaRadians)
        return minOf(difference, (PI.toFloat() - difference))
    }
}

