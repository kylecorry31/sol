package com.kylecorry.sol.math

import com.kylecorry.sol.shared.Guards
import kotlin.math.ceil
import kotlin.math.min

fun <T> Iterable<T>.sumOfFloat(selector: (value: T) -> Float): Float {
    return this.sumOf { selector(it).toDouble() }.toFloat()
}

fun Pair<Float, Float>.toVector2(): Vector2 {
    return Vector2(first, second)
}

fun <T> List<T>.split(percent: Float): Pair<List<T>, List<T>> {
    val n = ceil(size * percent).toInt()
    return subList(0, n) to subList(n, size)
}

fun <T> List<T>.batch(n: Int): List<List<T>> {
    Guards.isPositive(n, "n")

    val lists = mutableListOf<List<T>>()
    var start = 0
    while (start < size) {
        val end = min(start + n, size)
        lists.add(subList(start, end))
        start = end
    }
    return lists
}