package com.kylecorry.sol.math.filters

class MovingAverageFilter1D(var size: Int) : IFilter1D {
    override fun filter(data: List<Float>): List<Float> {
        val filter = MovingAverageFilter(size)
        return data.map { filter.filter(it) }
    }
}