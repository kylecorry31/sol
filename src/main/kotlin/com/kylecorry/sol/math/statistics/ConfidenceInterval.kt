package com.kylecorry.sol.math.statistics

data class ConfidenceInterval<T>(val value: T, val lower: T, val upper: T, val confidence: Float)
