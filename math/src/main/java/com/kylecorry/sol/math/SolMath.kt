package com.kylecorry.sol.math

import com.kylecorry.sol.math.filters.LowPassFilter
import com.kylecorry.sol.math.filters.MovingAverageFilter
import kotlin.math.*

object SolMath {

    fun wrap(value: Float, min: Float, max: Float): Float {
        return wrap(value.toDouble(), min.toDouble(), max.toDouble()).toFloat()
    }

    fun wrap(value: Double, min: Double, max: Double): Double {

        if (min >= max){
            return value
        }

        val range = max - min

        var newValue = value

        while (newValue > max) {
            newValue -= range
        }

        while (newValue < min) {
            newValue += range
        }

        return newValue
    }

    fun power(x: Int, power: Int): Int {
        if (x == 1){
            return 1
        }


        var total = 1
        for (i in 0 until abs(power)) {
            total *= x
        }

        if (power < 0) {
            return 0
        }

        return total
    }

    fun power(x: Double, power: Int): Double {
        var total = 1.0
        for (i in 0 until abs(power)) {
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
        for (i in coefs.indices) {
            runningTotal += power(x, i) * coefs[i]
        }

        return runningTotal
    }

    fun cube(a: Double): Double {
        return a * a * a
    }

    fun square(a: Double): Double {
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
        var delta = angle2 - angle1
        delta += 180
        delta -= floor(delta / 360) * 360
        delta -= 180
        if (abs(abs(delta) - 180) <= Float.MIN_VALUE) {
            delta = 180f
        }
        return delta
    }

    fun clamp(value: Double, minimum: Double, maximum: Double): Double {
        return value.coerceIn(minimum, maximum)
    }

    fun clamp(value: Float, minimum: Float, maximum: Float): Float {
        return value.coerceIn(minimum, maximum)
    }

    fun Double.roundPlaces(places: Int): Double {
        return (this * 10.0.pow(places)).roundToInt() / 10.0.pow(places)
    }

    fun Float.roundPlaces(places: Int): Float {
        return (this * 10f.pow(places)).roundToInt() / 10f.pow(places)
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

        return data.map { filter.filter(it.toDouble()).toFloat() }
    }

    /**
     * Calculates the slope of the best fit line
     */
    fun slope(data: List<Pair<Float, Float>>): Float {
        if (data.isEmpty()) {
            return 0f
        }

        val xBar = data.map { it.first }.average().toFloat()
        val yBar = data.map { it.second }.average().toFloat()

        var ssxx = 0.0f
        var ssxy = 0.0f
        var ssto = 0.0f

        for (i in data.indices) {
            ssxx += (data[i].first - xBar).pow(2)
            ssxy += (data[i].first - xBar) * (data[i].second - yBar)
            ssto += (data[i].second - yBar).pow(2)
        }

        return ssxy / ssxx
    }

    fun List<Double>.toDoubleArray(): DoubleArray {
        return DoubleArray(size) {
            get(it)
        }
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

    fun lerp(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    fun lerp(start: Double, end: Double, percent: Double): Double {
        return start + (end - start) * percent
    }

    fun map(
        value: Double,
        originalMin: Double,
        originalMax: Double,
        newMin: Double,
        newMax: Double
    ): Double {
        val normal = norm(value, originalMin, originalMax)
        return lerp(newMin, newMax, normal)
    }

    fun map(
        value: Float,
        originalMin: Float,
        originalMax: Float,
        newMin: Float,
        newMax: Float
    ): Float {
        val normal = norm(value, originalMin, originalMax)
        return lerp(newMin, newMax, normal)
    }

    fun norm(value: Double, minimum: Double, maximum: Double): Double {
        val range = maximum - minimum
        if (range == 0.0) {
            return 0.0
        }
        return (value - minimum) / range
    }

    fun norm(value: Float, minimum: Float, maximum: Float): Float {
        val range = maximum - minimum
        if (range == 0f) {
            return 0f
        }
        return (value - minimum) / range
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

}