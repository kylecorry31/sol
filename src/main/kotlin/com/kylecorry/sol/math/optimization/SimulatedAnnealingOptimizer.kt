package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.interpolation.Interpolation.lerp
import kotlin.math.exp
import kotlin.random.Random

class SimulatedAnnealingOptimizer(
    private val startingTemperature: Double,
    private val stepSize: Double,
    private val maxIterations: Int = 1000,
    private val minimumTemperature: Double = 0.0,
    private val initialValue: Pair<Double, Double>? = null,
    private val coolingFn: (t0: Double, t: Double, k: Int) -> Double = { t0: Double, _: Double, k: Int ->
        t0 / (k + 1).toDouble()
    }
) : IOptimizer {

    private val random = Random(1)

    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double> {
        val myFn = { x: Double, y: Double -> if (maximize) -fn(x, y) else fn(x, y) }

        var bestX = initialValue?.first ?: lerp(random.nextDouble(), xRange.start, xRange.end)
        var bestY = initialValue?.second ?: lerp(random.nextDouble(), yRange.start, yRange.end)
        var bestZ = myFn(bestX, bestY)

        var x = bestX
        var y = bestY
        var z = bestZ

        var t = startingTemperature

        for (i in 0 until maxIterations) {
            if (t < minimumTemperature) {
                break
            }
            val newX = x + random.nextDouble(-1.0, 1.0) * stepSize
            val newY = y + random.nextDouble(-1.0, 1.0) * stepSize
            val newZ = myFn(newX, newY)

            if (newZ < bestZ) {
                bestX = newX
                bestY = newY
                bestZ = newZ
            }

            val diff = newZ - z

            t = coolingFn(startingTemperature, t, i)

            val metropolisAC = exp(-diff / t)

            if (diff < 0 || random.nextDouble() < metropolisAC) {
                x = newX
                y = newY
                z = newZ
            }
        }
        return bestX to bestY
    }
}