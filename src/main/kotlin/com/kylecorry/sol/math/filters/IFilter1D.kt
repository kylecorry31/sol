package com.kylecorry.sol.math.filters

interface IFilter1D {
    fun filter(data: List<Float>): List<Float>
}