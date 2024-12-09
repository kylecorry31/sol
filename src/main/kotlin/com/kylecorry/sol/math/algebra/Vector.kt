package com.kylecorry.sol.math.algebra

import kotlin.math.sqrt

typealias Vector = Array<Float>

fun Vector.toColumnMatrix(): Matrix {
    return createMatrix(size, 1) { row, _ ->
        this[row]
    }
}

fun Vector.toRowMatrix(): Matrix {
    return createMatrix(1, size) { _, col ->
        this[col]
    }
}

fun Vector.magnitude(): Float {
    return norm()
}

fun Vector.norm(): Float {
    return sqrt(sumOf { it.toDouble() * it }).toFloat()
}

fun Vector.minus(other: Vector): Vector {
    return mapIndexed { index, value ->
        value - other[index]
    }.toTypedArray()
}

fun Vector.plus(other: Vector): Vector {
    return mapIndexed { index, value ->
        value + other[index]
    }.toTypedArray()
}