package com.kylecorry.sol.math.classifiers

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.algebra.Matrix
import com.kylecorry.sol.math.algebra.createMatrix

interface IClassifier {

    /**
     * Classifies the input values into a probability distribution by class
     */
    fun classify(x: List<Float>): List<Float>

}

/**
 * Creates a confusion matrix for the provided data
 */
fun IClassifier.confusion(
    classes: Int,
    x: List<List<Float>>,
    y: List<Int>
): Matrix {
    val matrix = createMatrix(classes, classes, 0f)
    x.zip(y).forEach {
        val prediction = SolMath.argmax(this.classify(it.first))
        matrix[prediction][it.second]++
    }
    return matrix
}