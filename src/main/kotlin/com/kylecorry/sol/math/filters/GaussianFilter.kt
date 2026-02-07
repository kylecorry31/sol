package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.RingBuffer
import com.kylecorry.sol.math.MathExtensions.positive
import com.kylecorry.sol.math.MathExtensions.real
import com.kylecorry.sol.math.statistics.GaussianDistribution
import com.kylecorry.sol.math.statistics.Statistics

class GaussianFilter(samples: Int, private val defaultError: Float) : IFilter {

    private val buffer = RingBuffer<GaussianDistribution>(samples)

    var value: Float = 0f
        private set

    var error: Float? = null
        private set

    val hasValidReading: Boolean
        get() = buffer.isFull()

    fun filter(measurement: Float, error: Float): Float {
        val variance = error
            .real(defaultError)
            .positive(defaultError)

        val distribution = GaussianDistribution(
            measurement.real(0f),
            variance
        )

        buffer.add(distribution)
        val calculated = Statistics.joint(buffer.toList())
        if (calculated != null) {
            this.value = calculated.mean.real(0f)
            this.error =
                calculated.standardDeviation.real(defaultError).positive(defaultError)
        }
        return this.value
    }

    override fun filter(measurement: Float): Float {
        return filter(measurement, defaultError)
    }
}