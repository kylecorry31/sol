package com.kylecorry.sol.science.shared

data class DichotomousKey<T>(
    val label: String,
    val yes: DichotomousKey<T>?,
    val no: DichotomousKey<T>?,
    val value: T?
) {
    companion object {
        fun <T> question(
            label: String,
            left: DichotomousKey<T>,
            right: DichotomousKey<T>
        ): DichotomousKey<T> {
            return DichotomousKey(label, left, right, null)
        }

        fun <T> answer(label: String, value: T): DichotomousKey<T> {
            return DichotomousKey(label, null, null, value)
        }

        fun answer(value: String): DichotomousKey<String> {
            return answer(value, value)
        }
    }
}