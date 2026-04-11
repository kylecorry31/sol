package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.trigonometry.Trigonometry
import kotlin.math.abs

data class Size(val width: Float, val height: Float) {
    /**
     * Calculates the size of the rectangle when rotated (maximum bounds)
     * @param angle: The angle to rotate the rectangle by
     * @return The size of the rectangle when rotated
     */
    fun rotate(angle: Float): Size {
        val sinAngle = Trigonometry.sinDegrees(angle)
        val cosAngle = Trigonometry.cosDegrees(angle)
        return Size(
            abs(width * cosAngle) + abs(height * sinAngle),
            abs(width * sinAngle) + abs(height * cosAngle),
        )
    }
}