package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import kotlin.math.cos
import kotlin.math.sin

data class PolarLine(val rho: Float, val thetaRadians: Float) {
    private val cosTheta = cos(thetaRadians)
    private val sinTheta = sin(thetaRadians)

    fun evaluate(point: Vector2): Float {
        return point.x * cosTheta + point.y * sinTheta - rho
    }
}

