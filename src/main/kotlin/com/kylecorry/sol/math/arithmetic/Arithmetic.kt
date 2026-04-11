package com.kylecorry.sol.math.arithmetic

import com.kylecorry.sol.math.RoundingMethod
import kotlin.math.*

object Arithmetic {
    const val EPSILON_FLOAT = 1e-5f
    const val EPSILON_DOUBLE = 1e-10

    fun factorial(n: Int): Long {
        if (n == 0) {
            return 1
        }
        var result = 1L
        for (i in 2..n.absoluteValue) {
            result *= i
        }
        return result * n.sign
    }

    fun wrap(
        value: Float,
        min: Float,
        max: Float,
    ): Float = wrap(value.toDouble(), min.toDouble(), max.toDouble()).toFloat()

    fun wrap(
        value: Double,
        min: Double,
        max: Double,
    ): Double {
        // https://stackoverflow.com/questions/14415753/wrap-value-into-range-min-max-without-division
        val range = max - min
        if (value < min) {
            return max - (min - value) % range
        }

        if (value > max) {
            return min + (value - min) % range
        }

        return value
    }

    fun power(
        x: Int,
        power: Int,
    ): Int {
        if (x == 1) {
            return 1
        }

        if (power < 0) {
            return 0
        }

        var total = 1
        for (i in 0 until power) {
            total *= x
        }

        return total
    }

    fun power(
        x: Double,
        power: Int,
    ): Double {
        var total = 1.0
        for (i in 0..<abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 1 / total
        }

        return total
    }

    fun power(
        x: Float,
        power: Int,
    ): Float {
        var total = 1f
        for (i in 0..<abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 1 / total
        }

        return total
    }

    fun cube(a: Double): Double = a * a * a

    fun square(a: Double): Double = a * a

    fun cube(a: Float): Float = a * a * a

    fun square(a: Float): Float = a * a

    fun clamp(
        value: Double,
        minimum: Double,
        maximum: Double,
    ): Double = value.coerceIn(minimum, maximum)

    fun clamp(
        value: Float,
        minimum: Float,
        maximum: Float,
    ): Float = value.coerceIn(minimum, maximum)

    fun isCloseTo(
        a: Double,
        b: Double,
        tolerance: Double,
    ): Boolean = (a - b).absoluteValue <= tolerance

    fun isCloseTo(
        a: Float,
        b: Float,
        tolerance: Float,
    ): Boolean = (a - b).absoluteValue <= tolerance

    fun greatestCommonDivisor(
        a: Long,
        b: Long,
    ): Long {
        val maxIterations = 1000
        var currentA = a
        var currentB = b
        var iterations = 0
        while (currentB != 0L && iterations < maxIterations) {
            val temp = currentB
            currentB = currentA % currentB
            currentA = temp
            iterations++
        }
        return currentA
    }

    fun greatestCommonDivisor(
        a: Double,
        b: Double,
        precision: Double = 0.0001,
    ): Double {
        val maxIterations = 1000
        var currentA = a
        var currentB = b
        var iterations = 0
        while (currentB.absoluteValue > precision && iterations < maxIterations) {
            val temp = currentB
            currentB = currentA % currentB
            currentA = temp
            iterations++
        }
        return currentA
    }

    fun greatestCommonDivisor(
        a: Float,
        b: Float,
    ): Float = greatestCommonDivisor(a.toDouble(), b.toDouble()).toFloat()

    fun greatestCommonDivisor(
        a: Int,
        b: Int,
    ): Int = greatestCommonDivisor(a.toLong(), b.toLong()).toInt()

    fun leastCommonMultiple(
        a: Long,
        b: Long,
    ): Long {
        if (a == 0L || b == 0L) {
            return 0
        }
        return abs(a) * (abs(b) / greatestCommonDivisor(a, b))
    }

    fun leastCommonMultiple(
        a: Int,
        b: Int,
    ): Int = leastCommonMultiple(a.toLong(), b.toLong()).toInt()

    fun leastCommonMultiple(
        a: Double,
        b: Double,
    ): Double {
        if (a == 0.0 || b == 0.0) {
            return 0.0
        }
        return abs(a) * (abs(b) / greatestCommonDivisor(a, b))
    }

    fun leastCommonMultiple(
        a: Float,
        b: Float,
    ): Float = leastCommonMultiple(a.toDouble(), b.toDouble()).toFloat()

    fun isApproximatelyEqual(
        a: Float,
        b: Float,
        tolerance: Float = EPSILON_FLOAT,
    ): Boolean = isZero(a - b, tolerance)

    fun isZero(
        value: Float,
        tolerance: Float = EPSILON_FLOAT,
    ): Boolean = abs(value) < tolerance

    fun roundPlaces(
        value: Double,
        places: Int,
    ): Double = (value * 10.0.pow(places)).roundToLong() / 10.0.pow(places)

    fun roundPlaces(
        value: Float,
        places: Int,
    ): Float = (value * 10f.pow(places)).roundToLong() / 10f.pow(places)

    fun roundNearest(
        value: Double,
        nearest: Double,
    ): Double = (value / nearest).roundToLong() * nearest

    fun roundNearest(
        value: Float,
        nearest: Float,
    ): Float = (value / nearest).roundToLong() * nearest

    fun roundNearest(
        value: Int,
        nearest: Int,
    ): Int = (value.toDouble() / nearest).roundToInt() * nearest

    fun round(
        value: Float,
        method: RoundingMethod,
    ): Int =
        when (method) {
            RoundingMethod.AwayFromZero -> {
                if (abs(value) % 1 >= 0.5f) {
                    (sign(value) * abs(value).roundToInt()).toInt()
                } else {
                    value.toInt()
                }
            }

            RoundingMethod.TowardZero -> {
                if (abs(value) % 1 <= 0.5f) {
                    value.toInt()
                } else {
                    value.roundToInt()
                }
            }
        }

    fun real(
        value: Float,
        defaultValue: Float = 0f,
    ): Float = if (value.isNaN() || value.isInfinite()) defaultValue else value

    fun positive(
        value: Float,
        zeroReplacement: Float = 0f,
    ): Float =
        if (value < 0) {
            -value
        } else if (isZero(value)) {
            zeroReplacement
        } else {
            value
        }

    fun negative(
        value: Float,
        zeroReplacement: Float = 0f,
    ): Float =
        if (value > 0) {
            -value
        } else if (isZero(value)) {
            zeroReplacement
        } else {
            value
        }
}
