package com.kylecorry.sol.math

import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.trigonometry.Trigonometry

object MathExtensions {

    fun Double.toRadians(): Double {
        return Trigonometry.toRadians(this)
    }

    fun Float.toRadians(): Float {
        return Trigonometry.toRadians(this)
    }

    fun Float.toDegrees(): Float {
        return Trigonometry.toDegrees(this)
    }

    fun Double.toDegrees(): Double {
        return Trigonometry.toDegrees(this)
    }

    fun Double.roundPlaces(places: Int): Double {
        return Arithmetic.roundPlaces(this, places)
    }

    fun Float.roundPlaces(places: Int): Float {
        return Arithmetic.roundPlaces(this, places)
    }

    fun Double.roundNearest(nearest: Double): Double {
        return Arithmetic.roundNearest(this, nearest)
    }

    fun Float.roundNearest(nearest: Float): Float {
        return Arithmetic.roundNearest(this, nearest)
    }

    fun Int.roundNearest(nearest: Int): Int {
        return Arithmetic.roundNearest(this, nearest)
    }

    fun Double.roundNearestAngle(nearest: Double): Double {
        return Trigonometry.roundNearestAngle(this, nearest)
    }

    fun Float.roundNearestAngle(nearest: Float): Float {
        return Trigonometry.roundNearestAngle(this, nearest)
    }

    fun Float.real(defaultValue: Float = 0f): Float {
        return Arithmetic.real(this, defaultValue)
    }

    fun Float.positive(zeroReplacement: Float = 0f): Float {
        return Arithmetic.positive(this, zeroReplacement)
    }

    fun Float.negative(zeroReplacement: Float = 0f): Float {
        return Arithmetic.negative(this, zeroReplacement)
    }

    fun Float.round(method: RoundingMethod): Int {
        return Arithmetic.round(this, method)
    }
}