package com.kylecorry.sol.shared

import com.kylecorry.sol.shared.ArrayUtils.swap
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class ArrayUtilsTest {

    @Test
    fun swap() {
        val arr = arrayOf(1, 2, 3)
        arr.swap(0, 2)
        assertArrayEquals(arrayOf(3, 2, 1), arr)

        arr.swap(1, 1)
        assertArrayEquals(arrayOf(3, 2, 1), arr)

        assertThrows<ArrayIndexOutOfBoundsException> {
            arr.swap(0, 3)
        }

        assertThrows<ArrayIndexOutOfBoundsException> {
            arr.swap(3, 0)
        }

        assertThrows<ArrayIndexOutOfBoundsException> {
            arr.swap(-1, 0)
        }
    }
}