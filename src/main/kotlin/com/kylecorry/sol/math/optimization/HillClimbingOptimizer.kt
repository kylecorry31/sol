package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.interpolation.Interpolation.lerp
import com.kylecorry.sol.math.trigonometry.Trigonometry.cosDegrees
import com.kylecorry.sol.math.trigonometry.Trigonometry.sinDegrees
import kotlin.random.Random

class HillClimbingOptimizer(
    private val step: Double = 1.0,
    private val maxIterations: Int = 1000,
    private val initialValue: Pair<Double, Double>? = null
) : IOptimizer {

    private val random = Random(1)

    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double> {
        val myFn = { pos: Pair<Double, Double> ->
            if (maximize) {
                -fn(pos.first, pos.second)
            } else {
                fn(pos.first, pos.second)
            }
        }
        val x = initialValue?.first ?: lerp(random.nextDouble(), xRange.start, xRange.end)
        val y = initialValue?.second ?: lerp(random.nextDouble(), yRange.start, yRange.end)
        var position = x to y
        var z = myFn(position)
        var bestNeighbor = getBestNeighbor(position, xRange, yRange, step, myFn)
        var bestNeighborZ = myFn(bestNeighbor)
        var i = 0

        while (bestNeighborZ < z && i <= maxIterations) {
            position = bestNeighbor
            z = bestNeighborZ
            bestNeighbor = getBestNeighbor(position, xRange, yRange, step, myFn)
            bestNeighborZ = myFn(bestNeighbor)
            i++
        }

        return position
    }

    private fun getBestNeighbor(
        position: Pair<Double, Double>,
        xRange: Range<Double>,
        yRange: Range<Double>,
        step: Double,
        fn: (pos: Pair<Double, Double>) -> Double
    ): Pair<Double, Double> {

        var minPosition = position
        var minValue = fn(position)

        for (angle in 0 until 360 step 90) {
            val x = position.first + cosDegrees(angle.toDouble()) * step
            val y = position.second + sinDegrees(angle.toDouble()) * step

            if (x !in xRange || y !in yRange) {
                continue
            }

            val z = fn(x to y)
            if (z < minValue) {
                minPosition = x to y
                minValue = z
            }

        }

        return minPosition
    }

}