package com.kylecorry.sol.math

import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.filters.MovingAverageFilter
import kotlin.math.*

object SolMath {

    val EPSILON_FLOAT = 1e-5f

    fun wrap(value: Float, min: Float, max: Float): Float {
        return wrap(value.toDouble(), min.toDouble(), max.toDouble()).toFloat()
    }

    fun wrap(value: Double, min: Double, max: Double): Double {
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

    fun power(x: Int, power: Int): Int {
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

    fun power(x: Double, power: Int): Double {
        var total = 1.0
        for (i in 0..<abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 1 / total
        }

        return total
    }

    fun power(x: Float, power: Int): Float {
        var total = 1f
        for (i in 0..<abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 1 / total
        }

        return total
    }

    /**
     * Computes a polynomial
     * Ex. 1 + 2x + 5x^2 + x^4
     * polynomial(x, 1, 2, 5, 0, 1)
     */
    fun polynomial(x: Double, vararg coefs: Double): Double {
        var runningTotal = 0.0
        var xPower = 1.0
        for (i in coefs.indices) {
            runningTotal += xPower * coefs[i]
            xPower *= x
        }

        return runningTotal
    }

    fun cube(a: Double): Double {
        return a * a * a
    }

    fun square(a: Double): Double {
        return a * a
    }

    fun cube(a: Float): Float {
        return a * a * a
    }

    fun square(a: Float): Float {
        return a * a
    }

    fun interpolate(
        n: Double,
        y1: Double,
        y2: Double,
        y3: Double
    ): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 + (n / 2.0) * (a + b + n * c)
    }

    fun sinDegrees(angle: Double): Double {
        return sin(angle.toRadians())
    }

    fun sinDegrees(angle: Float): Float {
        return sin(angle.toRadians())
    }

    fun tanDegrees(angle: Double): Double {
        return tan(angle.toRadians())
    }

    fun tanDegrees(angle: Float): Float {
        return tan(angle.toRadians())
    }

    fun cosDegrees(angle: Double): Double {
        return cos(angle.toRadians())
    }

    fun cosDegrees(angle: Float): Float {
        return cos(angle.toRadians())
    }

    fun Double.toRadians(): Double {
        return Math.toRadians(this)
    }

    fun Float.toRadians(): Float {
        return Math.toRadians(this.toDouble()).toFloat()
    }

    fun Float.toDegrees(): Float {
        return Math.toDegrees(this.toDouble()).toFloat()
    }

    fun Double.toDegrees(): Double {
        return Math.toDegrees(this)
    }

    fun deltaAngle(angle1: Float, angle2: Float): Float {
        // These will be at most 360 degrees apart, so normalize them to restrict that
        val a = normalizeAngle(angle1 - angle2)
        val b = normalizeAngle(angle2 - angle1)
        return if (a < b) {
            -a
        } else {
            b
        }
    }

    fun clamp(value: Double, minimum: Double, maximum: Double): Double {
        return value.coerceIn(minimum, maximum)
    }

    fun clamp(value: Float, minimum: Float, maximum: Float): Float {
        return value.coerceIn(minimum, maximum)
    }

    fun Double.roundPlaces(places: Int): Double {
        return (this * 10.0.pow(places)).roundToLong() / 10.0.pow(places)
    }

    fun Float.roundPlaces(places: Int): Float {
        return (this * 10f.pow(places)).roundToLong() / 10f.pow(places)
    }

    fun Double.roundNearest(nearest: Double): Double {
        return (this / nearest).roundToLong() * nearest
    }

    fun Float.roundNearest(nearest: Float): Float {
        return (this / nearest).roundToLong() * nearest
    }

    fun Int.roundNearest(nearest: Int): Int {
        return (this.toDouble() / nearest).roundToInt() * nearest
    }

    fun Double.roundNearestAngle(nearest: Double): Double {
        val normalized = normalizeAngle(this)
        return normalizeAngle(normalized.roundNearest(nearest))
    }

    fun Float.roundNearestAngle(nearest: Float): Float {
        val normalized = normalizeAngle(this)
        return normalizeAngle(normalized.roundNearest(nearest))
    }

    fun smooth(data: List<Float>, smoothing: Float = 0.5f): List<Float> {
        if (data.isEmpty()) {
            return data
        }

        val filter = LowPassFilter(smoothing, data.first())

        return data.mapIndexed { index, value ->
            if (index == 0) {
                value
            } else {
                filter.filter(value)
            }
        }
    }

    fun movingAverage(data: List<Float>, window: Int = 5): List<Float> {
        val filter = MovingAverageFilter(window)

        return data.map { filter.filter(it) }
    }

    fun removeOutliers(
        measurements: List<Double>,
        threshold: Double,
        replaceWithAverage: Boolean = false,
        replaceLast: Boolean = false
    ): List<Double> {
        if (measurements.size < 3) {
            return measurements
        }

        val filtered = mutableListOf(measurements.first())

        for (i in 1 until measurements.lastIndex) {
            val before = measurements[i - 1]
            val current = measurements[i]
            val after = measurements[i + 1]

            val last = if (replaceWithAverage) (before + after) / 2 else filtered.last()

            if (current - before > threshold && current - after > threshold) {
                filtered.add(last)
            } else if (current - before < -threshold && current - after < -threshold) {
                filtered.add(last)
            } else {
                filtered.add(current)
            }
        }

        if (replaceLast && abs(filtered.last() - measurements.last()) > threshold) {
            filtered.add(filtered.last())
        } else {
            filtered.add(measurements.last())
        }
        return filtered
    }

    fun lerp(percent: Float, start: Float, end: Float, shouldClamp: Boolean = false): Float {
        val value = start + (end - start) * percent

        return if (shouldClamp) {
            clamp(value, start, end)
        } else {
            value
        }
    }

    fun lerp(percent: Double, start: Double, end: Double, shouldClamp: Boolean = false): Double {
        val value = start + (end - start) * percent

        return if (shouldClamp) {
            clamp(value, start, end)
        } else {
            value
        }
    }

    fun map(
        value: Double,
        originalMin: Double,
        originalMax: Double,
        newMin: Double,
        newMax: Double,
        shouldClamp: Boolean = false
    ): Double {
        val normal = norm(value, originalMin, originalMax)
        return lerp(normal, newMin, newMax, shouldClamp)
    }

    fun map(
        value: Float,
        originalMin: Float,
        originalMax: Float,
        newMin: Float,
        newMax: Float,
        shouldClamp: Boolean = false
    ): Float {
        val normal = norm(value, originalMin, originalMax)
        return lerp(normal, newMin, newMax, shouldClamp)
    }

    fun norm(value: Double, minimum: Double, maximum: Double, shouldClamp: Boolean = false): Double {
        val range = maximum - minimum
        if (range == 0.0) {
            return 0.0
        }
        val normal = (value - minimum) / range

        return if (shouldClamp) {
            clamp(normal, 0.0, 1.0)
        } else {
            normal
        }
    }

    fun norm(value: Float, minimum: Float, maximum: Float, shouldClamp: Boolean = false): Float {
        val range = maximum - minimum
        if (SolMath.isZero(range)) {
            return 0f
        }
        val normal = (value - minimum) / range

        return if (shouldClamp) {
            clamp(normal, 0f, 1f)
        } else {
            normal
        }
    }

    fun scaleToFit(
        width: Float,
        height: Float,
        maxWidth: Float,
        maxHeight: Float
    ): Float {
        return min(maxWidth / width, maxHeight / height)
    }

    fun normalizeAngle(angle: Float): Float {
        return wrap(angle, 0f, 360f) % 360
    }

    fun normalizeAngle(angle: Double): Double {
        return wrap(angle, 0.0, 360.0) % 360
    }

    fun <T : Comparable<T>> argmax(values: List<T>): Int {
        if (values.isEmpty()) {
            return -1
        }

        var maxIndex = 0

        for (i in values.indices) {
            if (values[i] > values[maxIndex]) {
                maxIndex = i
            }
        }

        return maxIndex
    }

    fun <T : Comparable<T>> argmin(values: List<T>): Int {
        if (values.isEmpty()) {
            return -1
        }

        var minIndex = 0

        for (i in values.indices) {
            if (values[i] < values[minIndex]) {
                minIndex = i
            }
        }

        return minIndex
    }

    fun <T> oneHot(value: Int, classes: Int, on: T, off: T): List<T> {
        return List(classes) { if (it == value) on else off }
    }

    fun isCloseTo(a: Double, b: Double, tolerance: Double): Boolean {
        return (a - b).absoluteValue <= tolerance
    }

    fun isCloseTo(a: Float, b: Float, tolerance: Float): Boolean {
        return (a - b).absoluteValue <= tolerance
    }

    fun isIncreasingX(data: List<Vector2>): Boolean {
        for (i in 1 until data.size) {
            if (data[i].x < data[i - 1].x) {
                return false
            }
        }
        return true
    }

    fun <T> reorder(data: List<T>, indices: List<Int>, inverse: Boolean = false): List<T> {
        return if (inverse) {
            val newIndices = MutableList(indices.size) { it }
            for (i in indices.indices) {
                val index = indices[i]
                newIndices[index] = i
            }
            reorder(data, newIndices, false)
        } else {
            val newData = data.toMutableList()
            for (i in data.indices) {
                val index = indices[i]
                newData[i] = data[index]
            }
            newData
        }
    }

    fun <T : Comparable<T>> sortIndices(data: List<T>): List<Int> {
        return data.mapIndexed { index, value ->
            index to value
        }.sortedBy { it.second }.map { it.first }
    }

    fun <T : Comparable<T>> sortIndicesDescending(data: List<T>): List<Int> {
        return data.mapIndexed { index, value ->
            index to value
        }.sortedByDescending { it.second }.map { it.first }
    }

    fun Float.real(defaultValue: Float = 0f): Float {
        return if (this.isNaN() || this.isInfinite()) defaultValue else this
    }

    fun Float.positive(zeroReplacement: Float = 0f): Float {
        return if (this < 0) {
            -this
        } else if (isZero(this)) {
            zeroReplacement
        } else {
            this
        }
    }

    fun Float.negative(zeroReplacement: Float = 0f): Float {
        return if (this > 0) {
            -this
        } else if (isZero(this)) {
            zeroReplacement
        } else {
            this
        }
    }

    fun Float.round(method: RoundingMethod): Int {
        return when (method) {
            RoundingMethod.AwayFromZero -> {
                if (abs(this) % 1 >= 0.5f) {
                    (sign(this) * abs(this).roundToInt()).toInt()
                } else {
                    toInt()
                }
            }

            RoundingMethod.TowardZero -> {
                if (abs(this) % 1 <= 0.5f) {
                    toInt()
                } else {
                    roundToInt()
                }
            }
        }
    }

    fun greatestCommonDivisor(a: Long, b: Long): Long {
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

    fun greatestCommonDivisor(a: Double, b: Double, precision: Double = 0.0001): Double {
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

    fun greatestCommonDivisor(a: Float, b: Float): Float {
        return greatestCommonDivisor(a.toDouble(), b.toDouble()).toFloat()
    }

    fun greatestCommonDivisor(a: Int, b: Int): Int {
        return greatestCommonDivisor(a.toLong(), b.toLong()).toInt()
    }

    fun leastCommonMultiple(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) {
            return 0
        }
        return abs(a) * (abs(b) / greatestCommonDivisor(a, b))
    }

    fun leastCommonMultiple(a: Int, b: Int): Int {
        return leastCommonMultiple(a.toLong(), b.toLong()).toInt()
    }

    fun leastCommonMultiple(a: Double, b: Double): Double {
        if (a == 0.0 || b == 0.0) {
            return 0.0
        }
        return abs(a) * (abs(b) / greatestCommonDivisor(a, b))
    }

    fun leastCommonMultiple(a: Float, b: Float): Float {
        return leastCommonMultiple(a.toDouble(), b.toDouble()).toFloat()
    }

    fun toDegrees(degrees: Double, minutes: Double = 0.0, seconds: Double = 0.0): Double {
        val sign = if (degrees < 0 || minutes < 0 || seconds < 0) -1 else 1
        return sign * (degrees.absoluteValue + minutes.absoluteValue / 60 + seconds.absoluteValue / 3600)
    }

    fun toDegrees(degrees: Float, minutes: Float = 0f, seconds: Float = 0f): Float {
        val sign = if (degrees < 0 || minutes < 0 || seconds < 0) -1 else 1
        return sign * (degrees.absoluteValue + minutes.absoluteValue / 60 + seconds.absoluteValue / 3600)
    }

    fun isApproximatelyEqual(a: Float, b: Float, tolerance: Float = EPSILON_FLOAT): Boolean {
        return isZero(a - b, tolerance)
    }

    fun isZero(value: Float, tolerance: Float = EPSILON_FLOAT): Boolean {
        return abs(value) < tolerance
    }
}