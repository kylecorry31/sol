package com.kylecorry.sol.math

import com.kylecorry.sol.math.algebra.Matrix
import kotlin.collections.sumOf
import kotlin.math.sqrt

@JvmInline
value class Vector(val data: FloatArray) {

    val n: Int
        get() = data.size

    val size: Int
        get() = data.size

    operator fun get(index: Int): Float {
        return data[index]
    }

    operator fun set(index: Int, value: Float) {
        data[index] = value
    }

    operator fun times(scalar: Float): Vector {
        return Vector(FloatArray(n) { i -> data[i] * scalar })
    }

    operator fun plus(other: Vector): Vector {
        return Vector(FloatArray(n) { i -> data[i] + other.data[i] })
    }

    operator fun minus(other: Vector): Vector {
        return Vector(FloatArray(n) { i -> data[i] - other.data[i] })
    }

    fun magnitude(): Float {
        return norm()
    }

    fun norm(): Float {
        return sqrt(data.sumOf { it.toDouble() * it }).toFloat()
    }

    fun toColumnMatrix(): Matrix {
        return Matrix.column(values = data)
    }

    fun toRowMatrix(): Matrix {
        return Matrix.row(values = data)
    }

    companion object {
        fun from(vararg values: Float): Vector {
            return Vector(values)
        }

        fun create(size: Int): Vector {
            return Vector(FloatArray(size))
        }
    }
}