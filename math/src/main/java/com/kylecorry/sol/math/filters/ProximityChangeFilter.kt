package com.kylecorry.sol.math.filters

import kotlin.math.abs

class ProximityChangeFilter(private val changeThreshold: Float) {

    private var lastValid: Float = Float.NaN

    fun filter(measurement: Float): Float {
        if (lastValid.isNaN()) {
            lastValid = measurement
            return lastValid
        }

        val distance = abs(measurement - lastValid)
        if (distance >= changeThreshold) {
            lastValid = measurement
        }

        return lastValid
    }

}