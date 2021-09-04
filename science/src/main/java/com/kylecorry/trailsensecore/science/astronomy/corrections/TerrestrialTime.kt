package com.kylecorry.trailsensecore.science.astronomy.corrections

import com.kylecorry.trailsensecore.math.MathUtils

internal object TerrestrialTime {

    fun getDeltaT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return MathUtils.polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year - 2100)
    }

}