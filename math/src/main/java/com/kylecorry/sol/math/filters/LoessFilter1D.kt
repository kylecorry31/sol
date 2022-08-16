package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2

/**
 * A filter for smoothing data
 * @param span the percentage of the dataset to use for smoothing each point
 * @param robustnessIterations the number of iterations to do for the robustness step for outlier removal
 * @param accuracy the threshold to stop the robustness at (short circuit)
 */
class LoessFilter1D(
    private val span: Float = 0.3f,
    private val robustnessIterations: Int = 2,
    private val accuracy: Float = 1e-12f,
    private val minimumSpanSize: Int = 0,
    private val maximumSpanSize: Int = Int.MAX_VALUE
) : IFilter1D {


    override fun filter(data: List<Float>): List<Float> {

        val filter =
            LoessFilter2D(span, robustnessIterations, accuracy, minimumSpanSize, maximumSpanSize)
        return filter.filter(data.mapIndexed { index, value -> Vector2(index.toFloat(), value) })
            .map { it.y }

    }

}