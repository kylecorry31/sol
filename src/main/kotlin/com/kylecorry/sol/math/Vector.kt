package com.kylecorry.sol.math

import com.kylecorry.sol.math.algebra.Matrix
import kotlin.math.sqrt

@JvmInline
value class Vector(
    val data: FloatArray,
) {
    val n: Int
        get() = data.size

    val size: Int
        get() = data.size

    operator fun get(index: Int): Float = data[index]

    operator fun set(
        index: Int,
        value: Float,
    ) {
        data[index] = value
    }

    operator fun times(scalar: Float): Vector = Vector(FloatArray(n) { i -> data[i] * scalar })

    operator fun plus(other: Vector): Vector = Vector(FloatArray(n) { i -> data[i] + other.data[i] })

    operator fun minus(other: Vector): Vector = Vector(FloatArray(n) { i -> data[i] - other.data[i] })

    fun magnitude(): Float = norm()

    fun norm(): Float = sqrt(data.sumOf { it.toDouble() * it }).toFloat()

    fun toColumnMatrix(): Matrix = Matrix.column(values = data)

    fun toRowMatrix(): Matrix = Matrix.row(values = data)

    companion object {
        fun from(vararg values: Float): Vector = Vector(values)

        fun create(size: Int): Vector = Vector(FloatArray(size))
    }
}
