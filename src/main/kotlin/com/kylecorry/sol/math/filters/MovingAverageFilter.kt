package com.kylecorry.sol.math.filters

class MovingAverageFilter(var size: Int) : IFilter {

    private val window = mutableListOf<Float>()

    override fun filter(measurement: Float): Float {
        window.add(measurement)
        while (window.size > size && window.size > 0) {
            window.removeAt(0)
        }
        return window.average().toFloat()
    }
}