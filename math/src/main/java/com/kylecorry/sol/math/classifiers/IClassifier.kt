package com.kylecorry.sol.math.classifiers

interface IClassifier {

    /**
     * Classifies the input values into a probability distribution by class
     */
    fun classify(x: List<Float>): List<Float>

}