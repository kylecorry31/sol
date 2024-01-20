package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import kotlin.math.sqrt

class IterativeSimpleExtremaFinder(
    initialStep: Double,
    finalStep: Double,
    levels: Int = 2
) : IExtremaFinder {

    private val stepSizes = (1..levels).map {
        initialStep + (finalStep - initialStep) * (it - 1) / (levels - 1).toDouble()
    }

    private val initialFinder = SimpleExtremaFinder(initialStep)

    override fun find(range: Range<Double>, fn: (x: Double) -> Double): List<Extremum> {
        // Get the initial extremas
        var extremas = initialFinder.find(range, fn)

        // Fine tune the extremas
        for (level in 1..<stepSizes.size) {
            extremas = fineTune(extremas, fn, level)
        }
        return extremas
    }

    private fun fineTune(
        extremas: List<Extremum>,
        fn: (x: Double) -> Double,
        level: Int
    ): List<Extremum> {
        val nextExtremas = mutableListOf<Extremum>()
        for (extrema in extremas) {
            val nextRange = createRange(extrema, stepSizes[level - 1])
            nextExtremas.add(
                findLevel(
                    nextRange,
                    fn,
                    level,
                    extrema.isHigh
                )
            )
        }
        return nextExtremas
    }

    private fun createRange(extremum: Extremum, stepSize: Double): Range<Double> {
        val start = extremum.point.x - stepSize
        val end = extremum.point.x + stepSize
        return Range(start, end)
    }

    private fun findLevel(
        range: Range<Double>,
        fn: (x: Double) -> Double,
        level: Int,
        isHigh: Boolean
    ): Extremum {
        val step = stepSizes[level]
        var targetX = range.start
        var targetY = fn(targetX)

        var currentX = range.start + step

        while (currentX <= range.end) {
            val currentY = fn(currentX)
            if (isHigh && currentY > targetY) {
                targetX = currentX
                targetY = currentY
            } else if (!isHigh && currentY < targetY) {
                targetX = currentX
                targetY = currentY
            }

            // Always check the last
            if (currentX != range.end && currentX + step > range.end) {
                currentX = range.end
                continue
            }

            currentX += step
        }

        return Extremum(
            Vector2(targetX.toFloat(), targetY.toFloat()),
            isHigh
        )
    }
}