package com.kylecorry.sol.shared

object ArrayUtils {

    fun <T> Array<T>.swap(i: Int, j: Int) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }

}
