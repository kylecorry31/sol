package com.kylecorry.sol.math.filters

class DeadReckoning(initialPosition: Float = 0f) {

    var position: Float = initialPosition
        private set

    fun calculate(velocity: Float, dt: Float): Float {
        position += velocity * dt
        return position
    }
}