package com.kylecorry.trailsensecore.math

import com.kylecorry.trailsensecore.math.TSMath.tanDegrees

internal class HeightCalculator {

    fun calculate(distance: Float, inclination: Float, phoneHeight: Float): Float {

        if (inclination < 0 || inclination == 90f) {
            return 0f
        }

        val heightFromPhone = tanDegrees(inclination) * distance

        return heightFromPhone + phoneHeight
    }

}