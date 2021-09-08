package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range

interface IOptimizer {
    fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean = true,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double>
}