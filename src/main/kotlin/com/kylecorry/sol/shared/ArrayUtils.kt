package com.kylecorry.sol.shared

object ArrayUtils {

    fun <T : Any?> Array<T>.swap(i: Int, j: Int){
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }

}