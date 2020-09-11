package com.kylecorry.trailsensecore.domain.inclinometer

import com.kylecorry.trailsensecore.domain.math.tanDegrees

internal class HeightCalculator {

    fun calculate(distance: Float, inclination: Float, phoneHeight: Float): Float {

        if (inclination < 0 || inclination == 90f) {
            return 0f
        }

        val heightFromPhone = tanDegrees(inclination) * distance

        return heightFromPhone + phoneHeight
    }

}