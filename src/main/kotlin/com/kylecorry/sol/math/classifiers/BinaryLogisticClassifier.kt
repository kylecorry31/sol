package com.kylecorry.sol.math.classifiers

import kotlin.math.exp
import kotlin.math.ln

/**
 * @param means the training mean of each feature, used to standardize it
 * @param scales the training standard deviation of each feature, used to standardize it
 * @param weights the weight of each standardized feature
 * @param threshold the probability at or above which the condition is predicted
 */
class BinaryLogisticClassifier(
    private val means: FloatArray,
    private val scales: FloatArray,
    private val intercept: Float,
    private val weights: FloatArray,
    threshold: Float = 0.5f
) : IClassifier {

    private val thresholdLogit = ln(threshold / (1 - threshold))

    init {
        require(means.size == weights.size) { "Means and weights must have the same size" }
        require(scales.size == weights.size) { "Scales and weights must have the same size" }
        require(threshold > 0 && threshold < 1) { "Threshold must be between 0 and 1 exclusive" }
    }

    /**
     * @param x the unstandardized feature values
     * @return the log odds above the threshold, which is at least 0 when the condition is predicted
     */
    fun margin(x: List<Float>): Float {
        return logit(x) - thresholdLogit
    }

    fun predict(x: List<Float>): Boolean {
        return margin(x) >= 0
    }

    override fun classify(x: List<Float>): List<Float> {
        val probability = 1 / (1 + exp(-logit(x)))
        return listOf(1 - probability, probability)
    }

    private fun logit(x: List<Float>): Float {
        require(x.size == weights.size) { "Expected ${weights.size} features but got ${x.size}" }
        var logit = intercept
        for (i in weights.indices) {
            logit += weights[i] * (x[i] - means[i]) / scales[i]
        }
        return logit
    }
}
