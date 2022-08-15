package com.kylecorry.sol.math

fun <T> Iterable<T>.sumOfFloat(selector: (value: T) -> Float): Float {
    return this.sumOf { selector(it).toDouble() }.toFloat()
}

fun Pair<Float, Float>.toVector2(): Vector2 {
    return Vector2(first, second)
}