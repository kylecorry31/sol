package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.statistics.StatisticsService

class MedianFilter(val size: Int): IFilter {

    private val window = mutableListOf<Float>()
    private val statistics = StatisticsService()

    override fun filter(measurement: Float): Float {
        window.add(measurement)
        if (window.size > size) {
            window.removeAt(0)
        }
        return statistics.median(window.map { it })
    }
}