package com.kylecorry.trailsensecore.domain.math

import com.kylecorry.andromeda.core.math.power
import com.kylecorry.andromeda.core.math.wrap

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

    fun cube(a: Double): Double {
        return a * a * a
    }

    fun square(a: Double): Double {
        return a * a
    }

    fun reduceAngleDegrees(angle: Double): Double {
        return wrap(angle, 0.0, 360.0)
    }

    fun interpolate(
        n: Double,
        y1: Double,
        y2: Double,
        y3: Double
    ): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 + (n / 2.0) * (a + b + n * c)
    }

}