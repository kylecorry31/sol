package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.statistics.Statistics
import kotlin.math.max

class DispersionExtremaFinder(
    private val lag: Int,
    private val threshold: Float,
    private val influence: Float
) : IListExtremaFinder {

    override fun find(values: List<Float>): List<Extremum> {
        if (values.size <= lag) {
            return emptyList()
        }

        val valueCopy = values.toMutableList()

        var highPeakIndex: Int? = null
        var highPeakValue: Float? = null
        var lowPeakIndex: Int? = null
        var lowPeakValue: Float? = null

        val peaks = mutableListOf<Extremum>()
        val extremum: (Int, Boolean) -> Extremum = { idx, isHigh ->
            Extremum(Vector2(idx.toFloat(), values[idx]), isHigh)
        }

        for (index in lag..valueCopy.lastIndex) {
            val value = valueCopy[index]
            val sub = valueCopy.subList(max(index - lag, 0), index)
            val avg = Statistics.mean(sub)
            val sd = Statistics.stdev(sub)
            if (value - avg > sd * threshold) {
                if (highPeakValue == null || value > highPeakValue) {
                    highPeakIndex = index
                    highPeakValue = value
                }
                valueCopy[index] = influence * value + (1 - influence) * value
            } else if (highPeakIndex != null) {
                peaks.add(extremum(highPeakIndex, true))
                highPeakIndex = null
                highPeakValue = null
            }

            if (value - avg < -sd * threshold) {
                if (lowPeakValue == null || value < lowPeakValue) {
                    lowPeakIndex = index
                    lowPeakValue = value
                }
                valueCopy[index] = influence * value + (1 - influence) * value
            } else if (lowPeakIndex != null) {
                peaks.add(extremum(lowPeakIndex, false))
                lowPeakIndex = null
                lowPeakValue = null
            }
        }
        if (highPeakIndex != null) {
            peaks.add(extremum(highPeakIndex, true))
        }

        if (lowPeakIndex != null) {
            peaks.add(extremum(lowPeakIndex, false))
        }
        return peaks
    }

}