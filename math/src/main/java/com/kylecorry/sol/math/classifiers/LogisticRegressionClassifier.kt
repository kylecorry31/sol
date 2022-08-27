package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.createMatrix
import com.kylecorry.sol.math.algebra.dot
import com.kylecorry.sol.math.statistics.Statistics

/**
 * A logistic regression classifier
 * @param weights an RxC matrix where R is the length of X and C is the number of classes
 */
class LogisticRegressionClassifier(private val weights: Matrix) : IClassifier {

    override fun classify(x: List<Float>): List<Float> {
        val input = createMatrix(1, x.size) { _, c -> x[c] }
        val z = input.dot(weights)[0].toList()
        return Statistics.softmax(z)
    }
}