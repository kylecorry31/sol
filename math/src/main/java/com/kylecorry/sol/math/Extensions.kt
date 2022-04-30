package com.kylecorry.sol.math

fun <T> Iterable<T>.sumOfFloat(selector: (value: T) -> Float): Float {
    return this.sumOf { selector(it).toDouble() }.toFloat()
}