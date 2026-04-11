package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import kotlin.math.PI

data class Circle(
    val center: Vector2,
    val radius: Float,
) {
    fun circumference(): Float = (2 * PI * radius).toFloat()

    fun area(): Float = (PI * square(radius)).toFloat()

    fun contains(point: Vector2): Boolean = center.distanceTo(point) <= radius

    fun intersects(other: Circle): Boolean = center.distanceTo(other.center) <= (radius + other.radius)
}
