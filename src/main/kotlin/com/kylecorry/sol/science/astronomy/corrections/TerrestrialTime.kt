package com.kylecorry.sol.science.astronomy.corrections

import com.kylecorry.sol.math.arithmetic.Arithmetic.polynomial

internal object TerrestrialTime {

    fun getDeltaT(year: Number): Double {
        val t = (year.toDouble() - 2000) / 100.0
        return polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year.toDouble() - 2100)
    }

}