package com.kylecorry.sol.shared

import com.kylecorry.sol.math.SolMath.power
import kotlin.math.roundToInt

@JvmInline
value class FixedPoint32 internal constructor(val bits: Int) {
    constructor(value: Float, precision: Int) : this((value * getScale(precision)).roundToInt())
    constructor(value: Double, precision: Int) : this((value * getScale(precision)).roundToInt())

    fun toFloat(precision: Int): Float {
        return (bits / getScale(precision).toFloat())
    }

    fun toDouble(precision: Int): Double {
        return (bits / getScale(precision).toDouble())
    }
}

private fun getScale(precision: Int): Int {
    return when (precision) {
        0 -> 1
        1 -> 10
        2 -> 100
        3 -> 1000
        4 -> 10000
        5 -> 100000
        6 -> 1000000
        7 -> 10000000
        8 -> 100000000
        else -> power(10, precision + 1)
    }
}