package com.kylecorry.sol.math.filters

interface IFilter {
    fun filter(measurement: Float): Float
}