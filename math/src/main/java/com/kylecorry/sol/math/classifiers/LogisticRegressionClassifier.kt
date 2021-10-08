package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.algebra.LinearAlgebraService
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.createMatrix
import com.kylecorry.sol.math.statistics.StatisticsService

/**
 * A logistic regression classifier
 * @param weights an RxC matrix where R is the length of X and C is the number of classes
 */
class LogisticRegressionClassifier(private val weights: Matrix) : IClassifier {

    private val statistics = StatisticsService()
    private val linearAlgebra = LinearAlgebraService()

    override fun classify(x: List<Float>): List<Float> {
        val input = createMatrix(1, x.size) { _, c -> x[c] }
        val z = linearAlgebra.dot(input, weights)[0].toList()
        return statistics.softmax(z)
    }
}