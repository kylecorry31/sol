package com.kylecorry.trailsensecore.domain.astronomy.correcctions

import com.kylecorry.trailsensecore.domain.math.MathUtils

object TerrestrialTime {

    fun getDeltaT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return MathUtils.polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year - 2100)
    }

}