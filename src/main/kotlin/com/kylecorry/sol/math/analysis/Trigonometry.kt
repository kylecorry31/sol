package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.*

object Trigonometry {

    fun toRadians(angle: Double): Double {
        return Math.toRadians(angle)
    }

    fun toRadians(angle: Float): Float {
        return Math.toRadians(angle.toDouble()).toFloat()
    }

    fun toDegrees(angle: Double): Double {
        return Math.toDegrees(angle)
    }

    fun toDegrees(angle: Float): Float {
        return Math.toDegrees(angle.toDouble()).toFloat()
    }

    fun toDegrees(degrees: Double, minutes: Double = 0.0, seconds: Double = 0.0): Double {
        val sign = if (degrees < 0 || minutes < 0 || seconds < 0) -1 else 1
        return sign * (abs(degrees) + abs(minutes) / 60 + abs(seconds) / 3600)
    }

    fun toDegrees(degrees: Float, minutes: Float = 0f, seconds: Float = 0f): Float {
        val sign = if (degrees < 0 || minutes < 0 || seconds < 0) -1 else 1
        return sign * (abs(degrees) + abs(minutes) / 60 + abs(seconds) / 3600)
    }

    fun sinDegrees(angle: Double): Double {
        return sin(toRadians(angle))
    }

    fun sinDegrees(angle: Float): Float {
        return sin(toRadians(angle))
    }

    fun cosDegrees(angle: Double): Double {
        return cos(toRadians(angle))
    }

    fun cosDegrees(angle: Float): Float {
        return cos(toRadians(angle))
    }

    fun tanDegrees(angle: Double): Double {
        return tan(toRadians(angle))
    }

    fun tanDegrees(angle: Float): Float {
        return tan(toRadians(angle))
    }

    fun normalizeAngle(angle: Float): Float {
        return Arithmetic.wrap(angle, 0f, 360f) % 360
    }

    fun normalizeAngle(angle: Double): Double {
        return Arithmetic.wrap(angle, 0.0, 360.0) % 360
    }

    fun deltaAngle(angle1: Float, angle2: Float): Float {
        val a = normalizeAngle(angle1 - angle2)
        val b = normalizeAngle(angle2 - angle1)
        return if (a < b) {
            -a
        } else {
            b
        }
    }

    fun roundNearestAngle(angle: Double, nearest: Double): Double {
        val normalized = normalizeAngle(angle)
        return normalizeAngle(Arithmetic.roundNearest(normalized, nearest))
    }

    fun roundNearestAngle(angle: Float, nearest: Float): Float {
        val normalized = normalizeAngle(angle)
        return normalizeAngle(Arithmetic.roundNearest(normalized, nearest))
    }

    /**
     * Remap an angle from a unit circle to a circle with a different start and direction
     * @param angle the angle on the unit circle
     * @param start the start of the new circle (as an angle on the unit circle)
     * @param isCounterClockwise true if the new circle is counter clockwise
     * @return the angle on the new circle
     */
    fun remapUnitAngle(
        angle: Float,
        start: Float,
        isCounterClockwise: Boolean,
    ): Float {
        val directionMultiplier = if (isCounterClockwise) 1 else -1
        return normalizeAngle(directionMultiplier * (angle - start))
    }

    /**
     * Convert an angle to a unit circle angle
     * @param angle the angle on the circle
     * @param start the start of the circle (as an angle on the unit circle)
     * @param isCounterClockwise true if the circle is counter clockwise
     * @return the angle on the unit circle
     */
    fun toUnitAngle(angle: Float, start: Float, isCounterClockwise: Boolean): Float {
        val directionMultiplier = if (isCounterClockwise) 1 else -1

        return normalizeAngle(directionMultiplier * angle + start)
    }


    /**
     * Remap an angle from one circle to another
     * @param originalAngle the angle on the original circle
     * @param newStart the start of the new circle
     * @param newIsCounterClockwise true if the new circle is counter clockwise
     * @param originalStart the start of the original circle
     * @param originalIsCounterClockwise true if the original circle is counter clockwise
     * @return the angle on the new circle
     */
    fun remapAngle(
        originalAngle: Float,
        newStart: Float,
        newIsCounterClockwise: Boolean = false,
        originalStart: Float = 0f,
        originalIsCounterClockwise: Boolean = true
    ): Float {
        return remapUnitAngle(
            toUnitAngle(originalAngle, originalStart, originalIsCounterClockwise),
            newStart,
            newIsCounterClockwise
        )
    }

    /**
     * Connects two points with a waveform where one point is the high, and the other the low
     * @param first the first point
     * @param second the second point
     * @param approximateFrequency the approximate frequency that these two points should be connected with. Defaults to interpreting one as a high and the other as a low as a half period
     * @return the waveform which connects these two points
     */
    fun connect(first: Vector2, second: Vector2, approximateFrequency: Float? = null): Waveform {
        val period = second.x - first.x
        val deltaY = abs(first.y - second.y)
        val verticalShift = deltaY / 2 + min(first.y, second.y)
        var frequency = PI / period

        if (approximateFrequency != null) {
            val below =
                frequency * (Arithmetic.power(
                    2,
                    floor(log2(approximateFrequency / frequency)).toInt()
                ) + 1)
            val above = frequency * (Arithmetic.power(
                2,
                ceil(log2(approximateFrequency / frequency)).toInt()
            ) + 1)

            frequency = if (abs(approximateFrequency - below) < abs(approximateFrequency - above)) {
                below
            } else {
                above
            }
        }

        val amplitude = (if (first.y > second.y) 1 else -1) * deltaY / 2
        return CosineWave(amplitude, frequency.toFloat(), first.x, verticalShift)
    }

    fun getRangeY(waveform: Waveform): Range<Float> {
        return Range(-waveform.amplitude + waveform.verticalShift, waveform.amplitude + waveform.verticalShift)
    }

    fun getCombinationRangeY(waveforms: List<Waveform>): Range<Float> {
        val ranges = waveforms.map { getRangeY(it) }
        // TODO: To get a more accurate range, factor in the phase of the waveforms
        return Range(ranges.sumOfFloat { it.start }, ranges.sumOfFloat { it.end })
    }

    fun angularDistance(azimuth1: Float, altitude1: Float, azimuth2: Float, altitude2: Float): Float {
        val sinAlt1 = sinDegrees(altitude1)
        val cosAlt1 = cosDegrees(altitude1)
        val sinAlt2 = sinDegrees(altitude2)
        val cosAlt2 = cosDegrees(altitude2)
        return toDegrees(acos(sinAlt1 * sinAlt2 + cosAlt1 * cosAlt2 * cosDegrees(azimuth2 - azimuth1)))
    }

}