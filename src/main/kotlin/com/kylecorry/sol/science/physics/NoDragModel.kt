package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.Vector2

class NoDragModel : DragModel {
    override fun getDragAcceleration(velocity: Vector2): Vector2 {
        return Vector2.Companion.zero
    }
}