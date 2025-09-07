package com.kylecorry.sol.math

import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.shared.Guards
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

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

fun Float.floorToInt(): Int {
    return floor(this).toInt()
}

fun Float.ceilToInt(): Int {
    return ceil(this).toInt()
}

fun Float.safeRoundToInt(default: Int = 0): Int {
    return try {
        if (isNaN() || isInfinite()) {
            default
        } else {
            roundToInt()
        }
    } catch (_: Exception) {
        default
    }
}

fun Float.safeRoundPlaces(places: Int, default: Float = 0f): Float {
    return try {
        if (isNaN() || isInfinite()) {
            default
        } else {
            roundPlaces(places)
        }
    } catch (_: Exception) {
        default
    }
}

fun Double.safeRoundToInt(default: Int = 0): Int {
    return try {
        if (isNaN() || isInfinite()) {
            default
        } else {
            roundToInt()
        }
    } catch (_: Exception) {
        default
    }
}

fun Double.safeRoundPlaces(places: Int, default: Double = 0.0): Double {
    return try {
        if (isNaN() || isInfinite()) {
            default
        } else {
            roundPlaces(places)
        }
    } catch (_: Exception) {
        default
    }
}