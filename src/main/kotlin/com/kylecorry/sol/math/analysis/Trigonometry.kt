package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.deltaAngle
import com.kylecorry.sol.math.SolMath.normalizeAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.sumOfFloat
import kotlin.math.*

object Trigonometry {

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
                frequency * (SolMath.power(
                    2,
                    floor(log2(approximateFrequency / frequency)).toInt()
                ) + 1)
            val above = frequency * (SolMath.power(
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
        return acos(sinAlt1 * sinAlt2 + cosAlt1 * cosAlt2 * cosDegrees(azimuth2 - azimuth1)).toDegrees()
    }

}