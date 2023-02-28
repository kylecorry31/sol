package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2

data class Rectangle(val left: Float, val top: Float, val right: Float, val bottom: Float) {

    fun width(): Float {
        return right - left
    }

    fun height(): Float {
        return top - bottom
    }

    fun area(): Float {
        return width() * height()
    }

    fun contains(point: Vector2): Boolean {
        return point.x in left..right && point.y in bottom..top
    }
}