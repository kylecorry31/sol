package com.kylecorry.sol.math.filters

import kotlin.math.pow

class DeadReckoning(private val order: Int = 1, initialValue: Float = 0f) {

    private var current: Float = initialValue

    /**
     * Calculate the current position
     * @param value the value (rate of change)
     * @param dt the delta time (same time units as value)
     */
    fun calculate(value: Float, dt: Float): Float {
        var scale = 1f
        for (i in 1..(order + 1)) {
            current += scale * value * dt.pow(i)
            scale /= i + 1
        }
        return current
    }
}