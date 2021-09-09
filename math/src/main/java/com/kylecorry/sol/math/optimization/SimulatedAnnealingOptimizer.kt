package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath.lerp
import kotlin.math.exp
import kotlin.random.Random

class SimulatedAnnealingOptimizer(
    private val startingTemperature: Double,
    private val stepSize: Double,
    private val maxIterations: Int = 1000
) : IOptimizer {

    private val random = Random(1)

    override fun optimize(
        xRange: Range<Double>,
        yRange: Range<Double>,
        maximize: Boolean,
        fn: (x: Double, y: Double) -> Double
    ): Pair<Double, Double> {
        val myFn = { x: Double, y: Double -> if (maximize) -fn(x, y) else fn(x, y) }

        var bestX = lerp(random.nextDouble(), xRange.start, xRange.end)
        var bestY = lerp(random.nextDouble(), yRange.start, yRange.end)
        var bestZ = myFn(bestX, bestY)

        var x = bestX
        var y = bestY
        var z = bestZ


        for (i in 0 until maxIterations) {
            val newX = x + random.nextDouble(-1.0, 1.0) * stepSize
            val newY = y + random.nextDouble(-1.0, 1.0) * stepSize
            val newZ = myFn(newX, newY)

            if (newZ < bestZ) {
                bestX = newX
                bestY = newY
                bestZ = newZ
            }

            val diff = newZ - z

            val t = startingTemperature / (i + 1).toFloat()

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