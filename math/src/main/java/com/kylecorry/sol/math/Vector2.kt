package com.kylecorry.sol.math

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

    fun normalize(): Vector2 {
        val length = magnitude()
        return Vector2(x / length, y / length)
    }

    fun distanceTo(other: Vector2): Float {
        return (other - this).magnitude()
    }

}