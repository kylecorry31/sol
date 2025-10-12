package com.kylecorry.sol.math.arithmetic

import kotlin.math.absoluteValue
import kotlin.math.sign

object Arithmetic {
    fun factorial(n: Int): Long {
        if (n == 0) {
            return 1
        }
        var result = 1L
        for (i in 2..n.absoluteValue) {
            result *= i
        }
        return result * n.sign
    }
}