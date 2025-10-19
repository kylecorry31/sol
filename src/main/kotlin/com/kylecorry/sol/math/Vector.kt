package com.kylecorry.sol.math

@JvmInline
value class Vector(val data: FloatArray) {

    val n: Int
        get() = data.size

    operator fun get(index: Int): Float {
        return data[index]
    }

    operator fun times(scalar: Float): Vector {
        return Vector(FloatArray(n) { i -> data[i] * scalar })
    }

    operator fun plus(other: Vector): Vector {
        return Vector(FloatArray(n) { i -> data[i] + other.data[i] })
    }

    companion object {
        fun from(vararg values: Float): Vector {
            return Vector(values)
        }
    }
}