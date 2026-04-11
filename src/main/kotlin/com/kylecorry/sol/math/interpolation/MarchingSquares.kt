package com.kylecorry.sol.math.interpolation

import com.kylecorry.sol.shared.Executor
import com.kylecorry.sol.shared.SequentialExecutor
import kotlin.math.max
import kotlin.math.min

internal object MarchingSquares {

    fun <T> getIsoline(
        grid: List<List<Pair<T, Float>>>,
        threshold: Float,
        executor: Executor = SequentialExecutor(),
        interpolator: (percent: Float, a: T, b: T) -> T
    ): List<IsolineSegment<T>> {
        val calculators = mutableListOf<() -> List<IsolineSegment<T>>>()
        for (i in 0..<grid.size - 1) {
            for (j in 0..<grid[i].size - 1) {
                val square = listOf(
                    grid[i][j],
                    grid[i][j + 1],
                    grid[i + 1][j + 1],
                    grid[i + 1][j]
                )
                calculators.add {
                    marchingSquares(square, threshold, interpolator)
                }
            }
        }

        return executor.map(calculators).flatten()
    }

    private fun <T> marchingSquares(
        square: List<Pair<T, Float>>,
        threshold: Float,
        interpolator: (Float, T, T) -> T
    ): List<IsolineSegment<T>> {
        val contourLines = mutableListOf<IsolineSegment<T>>()


        /**
         *
         *   A--AB--B
         *   |      |
         *  AC     BD
         *   |      |
         *   C--CD--D
         */

        val a = square[0]
        val b = square[1]
        val c = square[3]
        val d = square[2]

        val ab = getInterpolatedPoint(threshold, a, b, interpolator)
        val ac = getInterpolatedPoint(threshold, a, c, interpolator)
        val bd = getInterpolatedPoint(threshold, b, d, interpolator)
        val cd = getInterpolatedPoint(threshold, c, d, interpolator)

        // If there are exactly 2 intersections, then there is 1 line
        val intersections = listOfNotNull(ab, ac, bd, cd)
        when (intersections.size) {
            2 -> {
                contourLines.add(IsolineSegment(intersections[0], intersections[1]))
            }
            4 if a.second >= threshold -> {
                contourLines.add(IsolineSegment(intersections[0], intersections[2]))
                contourLines.add(IsolineSegment(intersections[1], intersections[3]))
            }
            4 if a.second < threshold -> {
                contourLines.add(IsolineSegment(intersections[0], intersections[1]))
                contourLines.add(IsolineSegment(intersections[2], intersections[3]))
            }
        }
        return contourLines
    }

    private fun <T> getInterpolatedPoint(
        value: Float,
        a: Pair<T, Float>,
        b: Pair<T, Float>,
        interpolator: (Float, T, T) -> T
    ): T? {
        val aAbove = a.second >= value
        val bAbove = b.second >= value

        if (aAbove == bAbove) {
            return null
        }

        var pct = Interpolation.norm(value, min(a.second, b.second), max(a.second, b.second))
        if (a.second > b.second) {
            pct = 1 - pct
        }
        return interpolator(pct, a.first, b.first)
    }

}