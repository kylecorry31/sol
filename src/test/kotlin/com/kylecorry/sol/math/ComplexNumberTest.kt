package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ComplexNumberTest {

    @ParameterizedTest
    @CsvSource(
        "1, 1, 0.7853981633974483",
        "1, 0, 0.0",
        "0, 1, 1.5707963267948966",
        "-1, 0, 3.141592653589793",
        "0, -1, -1.5707963267948966",
        "-1, -1, -2.356194490192345"
    )
    fun getPhase(real: Float, imaginary: Float, expected: Float) {
        val complex = ComplexNumber(real, imaginary)
        assertEquals(expected, complex.phase, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1.4142135623730951",
        "1, 0, 1.0",
        "0, 1, 1.0",
        "-1, 0, 1.0",
        "0, -1, 1.0",
        "-1, -1, 1.4142135623730951"
    )
    fun getMagnitude(real: Float, imaginary: Float, expected: Float) {
        val complex = ComplexNumber(real, imaginary)
        assertEquals(expected, complex.magnitude, 0.0001f)
    }
}