package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.algebra.LinearEquation

data class Line(val start: Vector2, val end: Vector2) {

    val isVertical = start.x == end.x

    fun left(): Vector2 {
        return if (start.x < end.x) {
            start
        } else {
            end
        }
    }

    fun right(): Vector2 {
        return if (start.x < end.x) {
            end
        } else {
            start
        }
    }

    fun top(): Vector2 {
        return if (start.y < end.y) {
            end
        } else {
            start
        }
    }

    fun bottom(): Vector2 {
        return if (start.y < end.y) {
            start
        } else {
            end
        }
    }

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

    fun equation(): LinearEquation {
        return LinearEquation(slope(), intercept())
    }
}