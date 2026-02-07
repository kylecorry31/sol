package com.kylecorry.sol.math.filters

class LowPassFilter(var alpha: Float, initialValue: Float = 0f) : IFilter {

    var value = initialValue

    override fun filter(measurement: Float): Float {
        value = (1 - alpha) * value + alpha * measurement
        return value
    }
}