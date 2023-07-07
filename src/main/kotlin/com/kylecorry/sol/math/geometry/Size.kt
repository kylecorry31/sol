package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.SolMath
import kotlin.math.abs

data class Size(val width: Float, val height: Float) {
    /**
     * Calculates the size of the rectangle when rotated (maximum bounds)
     * @param angle: The angle to rotate the rectangle by
     * @return The size of the rectangle when rotated
     */
    fun rotate(angle: Float): Size {
        val sinAngle = SolMath.sinDegrees(angle)
        val cosAngle = SolMath.cosDegrees(angle)
        return Size(
            abs(width * cosAngle) + abs(height * sinAngle),
            abs(width * sinAngle) + abs(height * cosAngle),
        )
    }
}