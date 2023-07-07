package com.kylecorry.sol.math.filters

class MovingAverageFilter(var size: Int): IFilter {

    private val window = mutableListOf<Float>()

    override fun filter(measurement: Float): Float {
        window.add(measurement)
        if (window.size > size){
            window.removeAt(0)
        }
        return window.average().toFloat()
    }
}