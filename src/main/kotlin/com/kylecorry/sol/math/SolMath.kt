package com.kylecorry.sol.math

import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.filters.MovingAverageFilter
import com.kylecorry.sol.math.geometry.Geometry
import com.kylecorry.sol.math.interpolation.Interpolation
import kotlin.math.*

object SolMath {

    const val EPSILON_FLOAT = Arithmetic.EPSILON_FLOAT
    const val EPSILON_DOUBLE = Arithmetic.EPSILON_DOUBLE

    fun wrap(value: Float, min: Float, max: Float): Float {
        return Arithmetic.wrap(value, min, max)
    }

    fun wrap(value: Double, min: Double, max: Double): Double {
        return Arithmetic.wrap(value, min, max)
    }

    fun power(x: Int, power: Int): Int {
        return Arithmetic.power(x, power)
    }

    fun power(x: Double, power: Int): Double {
        return Arithmetic.power(x, power)
    }

    fun power(x: Float, power: Int): Float {
        return Arithmetic.power(x, power)
    }

    fun polynomial(x: Double, vararg coefs: Double): Double {
        return Arithmetic.polynomial(x, *coefs)
    }

    fun cube(a: Double): Double {
        return Arithmetic.cube(a)
    }

    fun square(a: Double): Double {
        return Arithmetic.square(a)
    }

    fun cube(a: Float): Float {
        return Arithmetic.cube(a)
    }

    fun square(a: Float): Float {
        return Arithmetic.square(a)
    }

    fun interpolate(
        n: Double,
        y1: Double,
        y2: Double,
        y3: Double
    ): Double {
        return Interpolation.catmullRom(n, y1, y2, y3)
    }

    fun sinDegrees(angle: Double): Double {
        return Trigonometry.sinDegrees(angle)
    }

    fun sinDegrees(angle: Float): Float {
        return Trigonometry.sinDegrees(angle)
    }

    fun tanDegrees(angle: Double): Double {
        return Trigonometry.tanDegrees(angle)
    }

    fun tanDegrees(angle: Float): Float {
        return Trigonometry.tanDegrees(angle)
    }

    fun cosDegrees(angle: Double): Double {
        return Trigonometry.cosDegrees(angle)
    }

    fun cosDegrees(angle: Float): Float {
        return Trigonometry.cosDegrees(angle)
    }

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

    fun deltaAngle(angle1: Float, angle2: Float): Float {
        return Trigonometry.deltaAngle(angle1, angle2)
    }

    fun clamp(value: Double, minimum: Double, maximum: Double): Double {
        return Arithmetic.clamp(value, minimum, maximum)
    }

    fun clamp(value: Float, minimum: Float, maximum: Float): Float {
        return Arithmetic.clamp(value, minimum, maximum)
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
        return Interpolation.lerp(percent, start, end, shouldClamp)
    }

    fun lerp(percent: Double, start: Double, end: Double, shouldClamp: Boolean = false): Double {
        return Interpolation.lerp(percent, start, end, shouldClamp)
    }

    fun map(
        value: Double,
        originalMin: Double,
        originalMax: Double,
        newMin: Double,
        newMax: Double,
        shouldClamp: Boolean = false
    ): Double {
        return Interpolation.map(value, originalMin, originalMax, newMin, newMax, shouldClamp)
    }

    fun map(
        value: Float,
        originalMin: Float,
        originalMax: Float,
        newMin: Float,
        newMax: Float,
        shouldClamp: Boolean = false
    ): Float {
        return Interpolation.map(value, originalMin, originalMax, newMin, newMax, shouldClamp)
    }

    fun norm(value: Double, minimum: Double, maximum: Double, shouldClamp: Boolean = false): Double {
        return Interpolation.norm(value, minimum, maximum, shouldClamp)
    }

    fun norm(value: Float, minimum: Float, maximum: Float, shouldClamp: Boolean = false): Float {
        return Interpolation.norm(value, minimum, maximum, shouldClamp)
    }

    fun scaleToFit(
        width: Float,
        height: Float,
        maxWidth: Float,
        maxHeight: Float
    ): Float {
        return Geometry.scaleToFit(width, height, maxWidth, maxHeight)
    }

    fun normalizeAngle(angle: Float): Float {
        return Trigonometry.normalizeAngle(angle)
    }

    fun normalizeAngle(angle: Double): Double {
        return Trigonometry.normalizeAngle(angle)
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
        return Arithmetic.isCloseTo(a, b, tolerance)
    }

    fun isCloseTo(a: Float, b: Float, tolerance: Float): Boolean {
        return Arithmetic.isCloseTo(a, b, tolerance)
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

    fun greatestCommonDivisor(a: Long, b: Long): Long {
        return Arithmetic.greatestCommonDivisor(a, b)
    }

    fun greatestCommonDivisor(a: Double, b: Double, precision: Double = 0.0001): Double {
        return Arithmetic.greatestCommonDivisor(a, b, precision)
    }

    fun greatestCommonDivisor(a: Float, b: Float): Float {
        return Arithmetic.greatestCommonDivisor(a, b)
    }

    fun greatestCommonDivisor(a: Int, b: Int): Int {
        return Arithmetic.greatestCommonDivisor(a, b)
    }

    fun leastCommonMultiple(a: Long, b: Long): Long {
        return Arithmetic.leastCommonMultiple(a, b)
    }

    fun leastCommonMultiple(a: Int, b: Int): Int {
        return Arithmetic.leastCommonMultiple(a, b)
    }

    fun leastCommonMultiple(a: Double, b: Double): Double {
        return Arithmetic.leastCommonMultiple(a, b)
    }

    fun leastCommonMultiple(a: Float, b: Float): Float {
        return Arithmetic.leastCommonMultiple(a, b)
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
        return Arithmetic.isApproximatelyEqual(a, b, tolerance)
    }

    fun isZero(value: Float, tolerance: Float = EPSILON_FLOAT): Boolean {
        return Arithmetic.isZero(value, tolerance)
    }
}