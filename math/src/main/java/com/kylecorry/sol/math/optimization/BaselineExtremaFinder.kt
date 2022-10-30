package com.kylecorry.sol.math.optimization

import com.kylecorry.sol.math.Vector2

class BaselineExtremaFinder : IListExtremaFinder {

    override fun find(values: List<Float>): List<Extremum> {
        if (values.isEmpty()) {
            return emptyList()
        }
        val baseline = values.average()
        val peaks = mutableListOf<Extremum>()
        var highPeakIndex: Int? = null
        var highPeakValue: Float? = null
        var lowPeakIndex: Int? = null
        var lowPeakValue: Float? = null

        val extremum: (Int, Boolean) -> Extremum = { idx, isHigh ->
            Extremum(Vector2(idx.toFloat(), values[idx]), isHigh)
        }

        values.forEachIndexed { index, value ->
            if (value > baseline) {
                if (highPeakValue == null || value > highPeakValue!!) {
                    highPeakIndex = index
                    highPeakValue = value
                }
            } else if (value < baseline && highPeakIndex != null) {
                peaks.add(extremum(highPeakIndex!!, true))
                highPeakIndex = null
                highPeakValue = null
            }

            if (value < baseline) {
                if (lowPeakValue == null || value < lowPeakValue!!) {
                    lowPeakIndex = index
                    lowPeakValue = value
                }
            } else if (value > baseline && lowPeakIndex != null) {
                peaks.add(extremum(lowPeakIndex!!, false))
                lowPeakIndex = null
                lowPeakValue = null
            }
        }
        if (highPeakIndex != null) {
            peaks.add(extremum(highPeakIndex!!, true))
        }

        if (lowPeakIndex != null) {
            peaks.add(extremum(lowPeakIndex!!, false))
        }
        return peaks
    }

}