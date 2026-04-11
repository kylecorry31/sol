package com.kylecorry.sol.math

data class Euler(
    val roll: Float,
    val pitch: Float,
    val yaw: Float,
) {
    fun toFloatArray(): FloatArray = floatArrayOf(roll, pitch, yaw)

    fun toQuaternion(): Quaternion = Quaternion.from(this)

    companion object {
        fun from(arr: FloatArray): Euler = Euler(arr[0], arr[1], arr[2])

        fun from(quaternion: Quaternion): Euler = quaternion.toEuler()
    }
}
