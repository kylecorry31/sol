package com.kylecorry.sol.math.statistics

data class TextureFeatures(
    val energy: Float,
    val entropy: Float,
    val contrast: Float,
    val homogeneity: Float,
    val dissimilarity: Float,
    val angularSecondMoment: Float,
    val horizontalMean: Float,
    val verticalMean: Float,
    val horizontalVariance: Float,
    val verticalVariance: Float,
    val correlation: Float,
    var max: Float
)
