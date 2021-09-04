package com.kylecorry.sol.science.astronomy.corrections

internal object TerrestrialTime {

    fun getDeltaT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return com.kylecorry.sol.math.SolMath.polynomial(t, 102.0, 102.0, 25.3) + 0.37 * (year - 2100)
    }

}