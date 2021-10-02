package com.kylecorry.sol.math.classifiers

import kotlin.math.exp

/**
 * A logistic regression classifier
 * @param weights an RxC list where R is the length of X and C is the number of classes
 */
class LogisticRegressionClassifier(private val weights: Array<Array<Float>>) : IClassifier {
    override fun classify(x: List<Float>): List<Float> {

        if (x.size != weights.size) {
            throw Exception("X must contain ${weights.size} items")
        }

        val z = Array(weights[0].size) { 0f }
        for (column in weights[0].indices) {
            var sum = 0f
            for (row in x.indices) {
                sum += weights[row][column] * x[row]
            }
            z[column] = sum
        }

        val maxZ = z.maxOrNull() ?: z[0]

        val exponents = z.map { exp(it - maxZ) }
        val sumExp = exponents.sum()

        return exponents.map { if (sumExp == 0f) 0f else it / sumExp }
    }
}