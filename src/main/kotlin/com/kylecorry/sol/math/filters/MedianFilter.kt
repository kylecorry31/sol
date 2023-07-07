package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.statistics.Statistics

class MedianFilter(val size: Int): IFilter {

    private val window = mutableListOf<Float>()

    override fun filter(measurement: Float): Float {
        window.add(measurement)
        if (window.size > size) {
            window.removeAt(0)
        }
        return Statistics.median(window.map { it })
    }
}