package com.kylecorry.sol.math

import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.normalizeAngle
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import kotlin.math.atan2
import kotlin.math.sqrt

@JvmInline
value class Vector2 internal constructor(internal val packed: Long) {

    constructor(x: Float, y: Float) : this(packXY(x, y))

    val x: Float
        get() = Float.fromBits(((packed ushr 32) and 0xFFFFFFFFL).toInt())

    val y: Float
        get() = Float.fromBits((packed and 0xFFFFFFFFL).toInt())

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
        if (Arithmetic.isZero(length)) return this
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
        if (Arithmetic.isZero(angle % 360f)) return this
        val x = this.x - origin.x
        val y = this.y - origin.y
        val cos = cosDegrees(angle)
        val sin = sinDegrees(angle)
        return Vector2(
            x * cos - y * sin + origin.x,
            x * sin + y * cos + origin.y
        )
    }

    fun copy(x: Float = this.x, y: Float = this.y): Vector2 {
        return Vector2(x, y)
    }

    override fun toString(): String {
        return "Vector2(x=$x, y=$y)"
    }

    companion object {
        val zero = from(0f, 0f)

        fun from(x: Float, y: Float): Vector2 {
            return Vector2(packXY(x, y))
        }
    }
}

private fun packXY(x: Float, y: Float): Long {
    val xb = x.toBits()
    val yb = y.toBits()
    return ((xb.toLong() and 0xFFFFFFFFL) shl 32) or (yb.toLong() and 0xFFFFFFFFL)
}
