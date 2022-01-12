package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range

interface IExtremaFinder {
    fun find(range: Range<Double>, fn: (x: Double) -> Double): List<Extremum>
}