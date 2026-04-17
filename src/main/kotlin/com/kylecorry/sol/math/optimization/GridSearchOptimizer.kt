package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range

class GridSearchOptimizer(private val stepSize: Double) : IOptimizer {
    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double> {
        var bestX = xRange.start
        var bestY = yRange.start
        var bestValue = fn(bestX, bestY)

        var x = xRange.start
        while (x <= xRange.end) {
            var y = yRange.start
            while (y <= yRange.end) {
                val value = fn(x, y)
                val isBetterMax = maximize && value > bestValue
                val isBetterMin = !maximize && value < bestValue
                if (isBetterMax || isBetterMin) {
                    bestValue = value
                    bestX = x
                    bestY = y
                }
                y += stepSize
            }
            x += stepSize
        }

        return Pair(bestX, bestY)
    }
}
