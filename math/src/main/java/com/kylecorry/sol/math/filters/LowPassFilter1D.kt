package com.kylecorry.sol.math.filters

class LowPassFilter1D(private val alpha: Float) : IFilter1D {
    override fun filter(data: List<Float>): List<Float> {
        if (data.isEmpty()) {
            return emptyList()
        }
        val filter = LowPassFilter(alpha, data.first())
        return data.map { filter.filter(it) }
    }
}