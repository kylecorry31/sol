package com.kylecorry.sol.math.algebra

import kotlin.math.sqrt

// TODO: Remove this in favor of the com.kylecorry.sol.math.Vector inline class
typealias Vector = Array<Float>

fun Vector.toColumnMatrix(): Matrix {
    return Matrix.create(size, 1) { row, _ ->
        this[row]
    }
}

fun Vector.toRowMatrix(): Matrix {
    return Matrix.create(1, size) { _, col ->
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