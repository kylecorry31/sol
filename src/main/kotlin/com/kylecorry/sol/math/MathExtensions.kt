package com.kylecorry.sol.math

import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.trigonometry.Trigonometry

object MathExtensions {
    fun Double.toRadians(): Double = Trigonometry.toRadians(this)

    fun Float.toRadians(): Float = Trigonometry.toRadians(this)

    fun Float.toDegrees(): Float = Trigonometry.toDegrees(this)

    fun Double.toDegrees(): Double = Trigonometry.toDegrees(this)

    fun Double.roundPlaces(places: Int): Double = Arithmetic.roundPlaces(this, places)

    fun Float.roundPlaces(places: Int): Float = Arithmetic.roundPlaces(this, places)

    fun Double.roundNearest(nearest: Double): Double = Arithmetic.roundNearest(this, nearest)

    fun Float.roundNearest(nearest: Float): Float = Arithmetic.roundNearest(this, nearest)

    fun Int.roundNearest(nearest: Int): Int = Arithmetic.roundNearest(this, nearest)

    fun Double.roundNearestAngle(nearest: Double): Double = Trigonometry.roundNearestAngle(this, nearest)

    fun Float.roundNearestAngle(nearest: Float): Float = Trigonometry.roundNearestAngle(this, nearest)

    fun Float.real(defaultValue: Float = 0f): Float = Arithmetic.real(this, defaultValue)

    fun Float.positive(zeroReplacement: Float = 0f): Float = Arithmetic.positive(this, zeroReplacement)

    fun Float.negative(zeroReplacement: Float = 0f): Float = Arithmetic.negative(this, zeroReplacement)

    fun Float.round(method: RoundingMethod): Int = Arithmetic.round(this, method)
}
