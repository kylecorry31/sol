package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2

data class Line(val start: Vector2, val end: Vector2) {
    fun length(): Float {
        return start.distanceTo(end)
    }

    /**
     * The slope of the line
     */
    fun slope(): Float {
        return (end.y - start.y) / (end.x - start.x)
    }

    /**
     * The Y intercept
     */
    fun intercept(): Float {
        return start.y - slope() * start.x
    }
}