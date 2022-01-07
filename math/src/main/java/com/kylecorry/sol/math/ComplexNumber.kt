package com.kylecorry.sol.math

import kotlin.math.atan2
import kotlin.math.hypot

data class ComplexNumber(val real: Float, val imaginary: Float) {
    val phase: Float
        get() = atan2(imaginary, real)

    val magnitude: Float
        get() = hypot(real, imaginary)
}