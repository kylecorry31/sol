package com.kylecorry.sol.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class ComplexNumber(val real: Float, val imaginary: Float) {
    val phase: Float
        get() = atan2(imaginary, real)

    val magnitude: Float
        get() = hypot(real, imaginary)

    operator fun plus(other: ComplexNumber): ComplexNumber {
        return ComplexNumber(real + other.real, imaginary + other.imaginary)
    }

    operator fun minus(other: ComplexNumber): ComplexNumber {
        return ComplexNumber(real - other.real, imaginary - other.imaginary)
    }

    operator fun times(other: ComplexNumber): ComplexNumber {
        return ComplexNumber(
            real * other.real - imaginary * other.imaginary,
            real * other.imaginary + imaginary * other.real
        )
    }

    operator fun times(other: Float): ComplexNumber {
        return ComplexNumber(real * other, imaginary * other)
    }

    companion object {

        /**
         * Calculates the complex exponential e^(i*theta)
         * @param theta The angle in radians
         * @return The complex number
         */
        fun exp(theta: Float): ComplexNumber {
            return ComplexNumber(cos(theta), sin(theta))
        }
    }
}