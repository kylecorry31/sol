package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.shared.Guards

class NoisyExtremaFinder(private val step: Double = 1.0, private val debounceCount: Int = 1) :
    IExtremaFinder, IListExtremaFinder {

    init {
        Guards.isPositive(step, "step")
    }

    override fun find(range: Range<Double>, fn: (x: Double) -> Double): List<Extremum> {
        val last = fn(range.start)
        var decreasing = last < fn(range.start - debounceCount * step)

        val extrema = mutableListOf<Extremum>()

        var count = 0
        var savedLevel = last

        var x = range.start
        var saved = x

        while (x <= range.end + (debounceCount * step)) {
            val level = fn(x)

            if (decreasing) {
                if (level < savedLevel) {
                    savedLevel = level
                    saved = x
                    count = 0
                } else {
                    count++
                }

                if (count > debounceCount) {
                    decreasing = false
                    count = 0
                    extrema.add(Extremum(Vector2(saved.toFloat(), savedLevel.toFloat()), false))
                }
            } else {
                if (level > savedLevel) {
                    savedLevel = level
                    saved = x
                    count = 0
                } else {
                    count++
                }

                if (count > debounceCount) {
                    decreasing = true
                    count = 0
                    extrema.add(Extremum(Vector2(saved.toFloat(), savedLevel.toFloat()), true))
                }
            }

            x += step
        }

        return extrema
    }

    override fun find(values: List<Float>): List<Extremum> {
        if (values.isEmpty()) {
            return emptyList()
        }
        val last = values.first()
        var decreasing = false

        val extrema = mutableListOf<Extremum>()

        var count = 0
        var savedLevel = last

        var x = 0
        var saved = 0

        while (x <= values.lastIndex) {
            val level = values[x]

            if (decreasing) {
                if (level < savedLevel) {
                    savedLevel = level
                    saved = x
                    count = 0
                } else {
                    count++
                }

                if (count > debounceCount) {
                    decreasing = false
                    count = 0
                    extrema.add(Extremum(Vector2(saved.toFloat(), savedLevel), false))
                }
            } else {
                if (level > savedLevel) {
                    savedLevel = level
                    saved = x
                    count = 0
                } else {
                    count++
                }

                if (count > debounceCount) {
                    decreasing = true
                    count = 0
                    extrema.add(Extremum(Vector2(saved.toFloat(), savedLevel), true))
                }
            }

            x += step.toInt()
        }
        return extrema
    }
}