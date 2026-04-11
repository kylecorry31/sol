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

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1, 1, 2, 2",
        "1, 1, 1, -1, 2, 0",
        "1, 1, -1, 1, 0, 2",
        "1, 1, -1, -1, 0, 0"
    )
    fun plus(real1: Float, imaginary1: Float, real2: Float, imaginary2: Float, expectedReal: Float, expectedImaginary: Float) {
        val complex1 = ComplexNumber(real1, imaginary1)
        val complex2 = ComplexNumber(real2, imaginary2)
        val expected = ComplexNumber(expectedReal, expectedImaginary)
        assertEquals(expected, complex1 + complex2)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1, 1, 0, 0",
        "1, 1, 1, -1, 0, 2",
        "1, 1, -1, 1, 2, 0",
        "1, 1, -1, -1, 2, 2"
    )
    fun minus(real1: Float, imaginary1: Float, real2: Float, imaginary2: Float, expectedReal: Float, expectedImaginary: Float) {
        val complex1 = ComplexNumber(real1, imaginary1)
        val complex2 = ComplexNumber(real2, imaginary2)
        val expected = ComplexNumber(expectedReal, expectedImaginary)
        assertEquals(expected, complex1 - complex2)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1, 1, 0, 2",
        "1, 1, 1, -1, 2, 0",
        "1, 1, -1, 1, -2, 0",
        "1, 1, -1, -1, 0, -2"
    )
    fun times(real1: Float, imaginary1: Float, real2: Float, imaginary2: Float, expectedReal: Float, expectedImaginary: Float) {
        val complex1 = ComplexNumber(real1, imaginary1)
        val complex2 = ComplexNumber(real2, imaginary2)
        val expected = ComplexNumber(expectedReal, expectedImaginary)
        assertEquals(expected, complex1 * complex2)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 2, 2, 2",
        "1, 1, -1, -1, -1",
        "1, 1, 0, 0, 0",
        "-1, 1, -1, 1, -1",
    )
    fun timesScalar(real: Float, imaginary: Float, scalar: Float, expectedReal: Float, expectedImaginary: Float) {
        val complex = ComplexNumber(real, imaginary)
        val expected = ComplexNumber(expectedReal, expectedImaginary)
        assertEquals(expected, complex * scalar)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1, 1, -1",
        "1, -1, 1, 1",
        "-1, 1, -1, -1",
        "-1, -1, -1, 1"
    )
    fun conjugate(real: Float, imaginary: Float, expectedReal: Float, expectedImaginary: Float) {
        val complex = ComplexNumber(real, imaginary)
        val expected = ComplexNumber(expectedReal, expectedImaginary)
        assertEquals(expected, complex.conjugate())
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1, 0",
        "3.141592653589793, -1, 0",
        "1.5707963267948966, 0, 1",
        "4.71238898038469, 0, -1",
        "6.283185307179586, 1, 0",
    )
    fun exp(theta: Float, expectedReal: Float, expectedImaginary: Float) {
        val expected = ComplexNumber(expectedReal, expectedImaginary)
        assertEquals(expected.real, ComplexNumber.exp(theta).real, 0.001f)
        assertEquals(expected.imaginary, ComplexNumber.exp(theta).imaginary, 0.001f)
    }
}