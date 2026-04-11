package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.Vector2

interface DragModel {
    fun getDragAcceleration(velocity: Vector2): Vector2
}