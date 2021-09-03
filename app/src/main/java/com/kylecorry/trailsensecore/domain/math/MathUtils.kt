package com.kylecorry.trailsensecore.domain.math

import com.kylecorry.andromeda.core.math.power

object MathUtils {

    /**
     * Computes a polynomial
     * Ex. 1 + 2x + 5x^2 + x^4
     * polynomial(x, 1, 2, 5, 0, 1)
     */
    fun polynomial(x: Double, vararg coefs: Double): Double {
        var runningTotal = 0.0
        for (i in coefs.indices) {
            runningTotal += power(x, i) * coefs[i]
        }

        return runningTotal
    }

}