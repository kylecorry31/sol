package com.kylecorry.sol.math.optimization

interface IListExtremaFinder {
    fun find(values: List<Float>): List<Extremum>
}