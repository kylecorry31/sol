package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import kotlin.math.min

class SimpleExtremaFinder(private val step: Double = 1.0) : IExtremaFinder, IListExtremaFinder {

    override fun find(range: Range<Double>, fn: (x: Double) -> Double): List<Extremum> {
        val extrema = mutableListOf<Extremum>()
        var previous = fn(range.start - step)
        var x = range.start
        var next = fn(x)
        while (x <= range.end) {
            val y = next
            next = fn(x + step)
            val isHigh = previous < y && next < y
            val isLow = previous > y && next > y

            if (isHigh) {
                extrema.add(Extremum(Vector2(x.toFloat(), y.toFloat()), true))
            }

            if (isLow) {
                extrema.add(Extremum(Vector2(x.toFloat(), y.toFloat()), false))
            }

            previous = y
            x += step
        }
        return extrema
    }

    override fun find(values: List<Float>): List<Extremum> {
        if (values.isEmpty()) {
            return emptyList()
        }
        val extrema = mutableListOf<Extremum>()
        var previous = values.first()
        var x = 0
        var next = previous
        while (x <= values.lastIndex) {
            val y = next
            val nextIdx = min(x + step.toInt(), values.lastIndex)
            next = values[nextIdx]
            val isHigh = previous < y && next < y
            val isLow = previous > y && next > y

            if (isHigh) {
                extrema.add(Extremum(Vector2(x.toFloat(), y), true))
            }

            if (isLow) {
                extrema.add(Extremum(Vector2(x.toFloat(), y), false))
            }

            previous = y
            x += step.toInt()
        }
        return extrema
    }

}