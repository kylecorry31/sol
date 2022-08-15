package com.kylecorry.sol.math.filters

class LowPassFilter(private val alpha: Float, initialValue: Float = 0f): IFilter {

    private var estimate = initialValue

    override fun filter(measurement: Float): Float {
        estimate = (1 - alpha) * estimate + alpha * measurement
        return estimate
    }
}