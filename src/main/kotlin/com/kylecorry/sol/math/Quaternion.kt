package com.kylecorry.sol.math

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import kotlin.math.*

data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) {

    private val arr = floatArrayOf(x, y, z, w)

    fun toFloatArray(): FloatArray {
        return arr.clone()
    }

    operator fun times(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.multiply(arr, other.arr, out)
        return from(out)
    }

    fun subtractRotation(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.subtractRotation(arr, other.arr, out)
        return from(out)
    }

    operator fun plus(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.add(arr, other.arr, out)
        return from(out)
    }

    operator fun minus(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.subtract(arr, other.arr, out)
        return from(out)
    }

    fun magnitude(): Float {
        return QuaternionMath.magnitude(arr)
    }

    fun normalize(): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.normalize(arr, out)
        return from(out)
    }

    fun conjugate(): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.conjugate(arr, out)
        return from(out)
    }

    fun inverse(): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.inverse(arr, out)
        return from(out)
    }

    fun rotate(vector: Vector3): Vector3 {
        val out = FloatArray(3)
        QuaternionMath.rotate(vector.toFloatArray(), arr, out)
        return Vector3.from(out)
    }

    fun toEuler(): Euler {
        val out = FloatArray(3)
        QuaternionMath.toEuler(arr, out)
        return Euler.from(out)
    }

    fun slerp(other: Quaternion, t: Float, useShortestPath: Boolean = true): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.slerp(arr, other.arr, t, out, useShortestPath)
        return from(out)
    }

    fun lerp(other: Quaternion, t: Float, useShortestPath: Boolean = true): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.lerp(arr, other.arr, t, out, useShortestPath)
        return from(out)
    }

    fun dot(other: Quaternion): Float {
        return QuaternionMath.dot(arr, other.arr)
    }

    companion object {
        val zero = Quaternion(0f, 0f, 0f, 1f)

        fun from(arr: FloatArray): Quaternion {
            return Quaternion(arr[0], arr[1], arr[2], arr[3])
        }

        fun from(euler: Euler): Quaternion {
            val out = FloatArray(4)
            QuaternionMath.fromEuler(euler.toFloatArray(), out)
            return from(out)
        }
    }

}


object QuaternionMath {
    const val X = 0
    const val Y = 1
    const val Z = 2
    const val W = 3

    fun fromEuler(euler: FloatArray, out: FloatArray){
        val cosY = cosDegrees(euler[2] / 2.0)
        val sinY = sinDegrees(euler[2] / 2.0)
        val cosP = cosDegrees(euler[1] / 2.0)
        val sinP = sinDegrees(euler[1] / 2.0)
        val cosR = cosDegrees(euler[0] / 2.0)
        val sinR = sinDegrees(euler[0] / 2.0)

        val w = cosR * cosP * cosY + sinR * sinP * sinY
        val x = sinR * cosP * cosY - cosR * sinP * sinY
        val y = cosR * sinP * cosY + sinR * cosP * sinY
        val z = cosR * cosP * sinY - sinR * sinP * cosY

        out[X] = x.toFloat()
        out[Y] = y.toFloat()
        out[Z] = z.toFloat()
        out[W] = w.toFloat()
    }

    fun rotate(point: FloatArray, quat: FloatArray, out: FloatArray) {
        val u = floatArrayOf(quat[X], quat[Y], quat[Z])
        val s = quat[W]
        val first = Vector3Utils.times(u, Vector3Utils.dot(u, point) * 2f)
        val second = Vector3Utils.times(point, s * s - Vector3Utils.dot(u, u))
        val third = Vector3Utils.times(Vector3Utils.cross(u, point), 2f * s)

        val sum = Vector3Utils.plus(first, Vector3Utils.plus(second, third))
        out[0] = sum[0]
        out[1] = sum[1]
        out[2] = sum[2]
    }

    fun toEuler(quat: FloatArray, out: FloatArray) {
        val yaw = atan2(
            2 * (quat[W] * quat[Z] + quat[X] * quat[Y]),
            1 - 2 * (quat[Y] * quat[Y] + quat[Z] * quat[Z])
        )
        val sinP = 2 * (quat[W] * quat[Y] - quat[Z] * quat[X])
        val pitch = if (sinP.absoluteValue >= 1) {
            (Math.PI / 2).withSign(sinP.toDouble()).toFloat()
        } else {
            asin(sinP)
        }
        val roll = atan2(
            2 * (quat[W] * quat[X] + quat[Y] * quat[Z]),
            1 - 2 * (quat[X] * quat[X] + quat[Y] * quat[Y])
        )

        out[0] = roll.toDegrees()
        out[1] = pitch.toDegrees()
        out[2] = yaw.toDegrees()
    }

    fun multiply(a: FloatArray, b: FloatArray, out: FloatArray) {
        val x = a[W] * b[X] + a[X] * b[W] + a[Y] * b[Z] - a[Z] * b[Y]
        val y = a[W] * b[Y] - a[X] * b[Z] + a[Y] * b[W] + a[Z] * b[X]
        val z = a[W] * b[Z] + a[X] * b[Y] - a[Y] * b[X] + a[Z] * b[W]
        val w = a[W] * b[W] - a[X] * b[X] - a[Y] * b[Y] - a[Z] * b[Z]
        out[X] = x
        out[Y] = y
        out[Z] = z
        out[W] = w
    }

    fun add(a: FloatArray, b: FloatArray, out: FloatArray) {
        out[X] = a[X] + b[X]
        out[Y] = a[Y] + b[Y]
        out[Z] = a[Z] + b[Z]
        out[W] = a[W] + b[W]
    }

    fun subtractRotation(a: FloatArray, b: FloatArray, out: FloatArray){
        val inverse = FloatArray(4)
        inverse(b, inverse)
        multiply(inverse, a, out)
        normalize(out, out)
    }

    fun subtract(a: FloatArray, b: FloatArray, out: FloatArray) {
        out[X] = a[X] - b[X]
        out[Y] = a[Y] - b[Y]
        out[Z] = a[Z] - b[Z]
        out[W] = a[W] - b[W]
    }

    fun magnitude(quat: FloatArray): Float {
        return sqrt(quat[X] * quat[X] + quat[Y] * quat[Y] + quat[Z] * quat[Z] + quat[W] * quat[W])
    }

    fun normalize(quat: FloatArray, out: FloatArray) {
        val mag = magnitude(quat)
        divide(quat, mag, out)
    }

    fun divide(quat: FloatArray, divisor: Float, out: FloatArray) {
        out[X] = quat[X] / divisor
        out[Y] = quat[Y] / divisor
        out[Z] = quat[Z] / divisor
        out[W] = quat[W] / divisor
    }

    fun multiply(quat: FloatArray, scale: Float, out: FloatArray) {
        out[X] = quat[X] * scale
        out[Y] = quat[Y] * scale
        out[Z] = quat[Z] * scale
        out[W] = quat[W] * scale
    }

    fun conjugate(quat: FloatArray, out: FloatArray) {
        out[X] = -quat[X]
        out[Y] = -quat[Y]
        out[Z] = -quat[Z]
        out[W] = quat[W]
    }

    fun inverse(quat: FloatArray, out: FloatArray) {
        val mag = magnitude(quat)
        conjugate(quat, out)
        divide(out, mag * mag, out)
    }

    fun slerp(quat1: FloatArray, quat2: FloatArray, t: Float, out: FloatArray, useShortestPath: Boolean = true){
        val cosHalfTheta = dot(quat1, quat2)
        if (cosHalfTheta.absoluteValue >= 1){
            out[X] = quat1[X]
            out[Y] = quat1[Y]
            out[Z] = quat1[Z]
            out[W] = quat1[W]
            return
        }

        if (useShortestPath && cosHalfTheta < 0){
            val temp = FloatArray(4)
            multiply(quat2, -1f, temp)
            // Passing in false for useShortestPath to avoid infinite recursion
            slerp(quat1, temp, t, out, false)
            return
        }


        val halfTheta = acos(cosHalfTheta)
        val sinHalfTheta = sqrt(1 - cosHalfTheta * cosHalfTheta)

        if (sinHalfTheta.absoluteValue < 0.001){
            out[X] = quat1[X] * 0.5f + quat2[X] * 0.5f
            out[Y] = quat1[Y] * 0.5f + quat2[Y] * 0.5f
            out[Z] = quat1[Z] * 0.5f + quat2[Z] * 0.5f
            out[W] = quat1[W] * 0.5f + quat2[W] * 0.5f
            return
        }

        val ratioA = sin((1 - t) * halfTheta) / sinHalfTheta
        val ratioB = sin(t * halfTheta) / sinHalfTheta

        out[X] = quat1[X] * ratioA + quat2[X] * ratioB
        out[Y] = quat1[Y] * ratioA + quat2[Y] * ratioB
        out[Z] = quat1[Z] * ratioA + quat2[Z] * ratioB
        out[W] = quat1[W] * ratioA + quat2[W] * ratioB
    }

    fun lerp(quat1: FloatArray, quat2: FloatArray, t: Float, out: FloatArray, useShortestPath: Boolean = true){
        val multiplier = if (useShortestPath && dot(quat1, quat2) < 0) -1f else 1f

        val x = quat1[X] + t * (multiplier * quat2[X] - quat1[X])
        val y = quat1[Y] + t * (multiplier * quat2[Y] - quat1[Y])
        val z = quat1[Z] + t * (multiplier * quat2[Z] - quat1[Z])
        val w = quat1[W] + t * (multiplier * quat2[W] - quat1[W])
        out[X] = x
        out[Y] = y
        out[Z] = z
        out[W] = w

        normalize(out, out)
    }

    fun dot(quat1: FloatArray, quat2: FloatArray): Float {
        return quat1[X] * quat2[X] + quat1[Y] * quat2[Y] + quat1[Z] * quat2[Z] + quat1[W] * quat2[W]
    }
}