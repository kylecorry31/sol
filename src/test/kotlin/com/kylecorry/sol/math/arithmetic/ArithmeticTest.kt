package com.kylecorry.sol.math.arithmetic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ArithmeticTest {
    @ParameterizedTest
    @CsvSource(
        "0, 1",
        "1, 1",
        "2, 2",
        "3, 6",
        "4, 24",
        "5, 120",
        "20, 2432902008176640000",
        "-1, -1",
        "-5, -120",
        "-20, -2432902008176640000"
    )
    fun factorial(n: Int, expected: Long) {
        assertEquals(expected, Arithmetic.factorial(n))
    }
}