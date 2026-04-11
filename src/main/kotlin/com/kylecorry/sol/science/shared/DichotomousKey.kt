package com.kylecorry.sol.science.shared

data class DichotomousKey<T>(
    val label: String,
    val yes: DichotomousKey<T>?,
    val no: DichotomousKey<T>?,
    val value: T?,
) {
    companion object {
        fun <T> question(
            label: String,
            yes: DichotomousKey<T>,
            no: DichotomousKey<T>,
        ): DichotomousKey<T> = DichotomousKey(label, yes, no, null)

        fun <T> answer(
            label: String,
            value: T,
        ): DichotomousKey<T> = DichotomousKey(label, null, null, value)

        fun answer(value: String): DichotomousKey<String> = answer(value, value)
    }
}
