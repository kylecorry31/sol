package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.Vector2
import kotlin.math.*

object Waves {

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

}