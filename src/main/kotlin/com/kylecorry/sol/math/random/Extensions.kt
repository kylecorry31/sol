package com.kylecorry.sol.math.random

import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

fun Random.nextGaussian(): Double {
    var v1: Double
    var v2: Double
    var s: Double
    repeat(128) {
        v1 = nextDouble(-1.0, 1.0)
        v2 = nextDouble(-1.0, 1.0)
        s = v1 * v1 + v2 * v2
        if (s < 1 && s != 0.0) {
            return v1 * sqrt(-2 * ln(s) / s)
        }
    }

    error("Unable to calculate nextGaussian after 128 attempts")
}
