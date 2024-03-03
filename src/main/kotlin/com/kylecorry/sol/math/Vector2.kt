package com.kylecorry.sol.math

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.normalizeAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import kotlin.math.atan2
import kotlin.math.sqrt

data class Vector2(val x: Float, val y: Float) {

    operator fun minus(other: Vector2): Vector2 {
        return Vector2(x - other.x, y - other.y)
    }

    operator fun plus(other: Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y)
    }

    operator fun times(scale: Float): Vector2 {
        return Vector2(x * scale, y * scale)
    }

    fun magnitude(): Float {
        return sqrt(x * x + y * y)
    }

    /**
     * The angle of the vector in degrees (0 - 360, unit circle)
     */
    fun angle(): Float {
        return normalizeAngle(atan2(y, x).toDegrees())
    }

    fun normalize(): Vector2 {
        val length = magnitude()
        if (length == 0f) return this
        return Vector2(x / length, y / length)
    }

    fun distanceTo(other: Vector2): Float {
        return (other - this).magnitude()
    }

    fun squaredMagnitude(): Float {
        return x * x + y * y
    }

    fun squaredDistanceTo(other: Vector2): Float {
        return (other - this).squaredMagnitude()
    }

    fun angleBetween(other: Vector2): Float {
        return (other - this).angle()
    }

    fun rotate(angle: Float, origin: Vector2 = zero): Vector2 {
        if (angle % 360f == 0f) return this
        val x = this.x - origin.x
        val y = this.y - origin.y
        val cos = cosDegrees(angle)
        val sin = sinDegrees(angle)
        return Vector2(
            x * cos - y * sin + origin.x,
            x * sin + y * cos + origin.y
        )
    }

    companion object {
        val zero = Vector2(0f, 0f)
    }

}