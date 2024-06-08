package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FastFourierTransformTest {

    @Test
    fun fft() {
        val data = listOf(0f, 1f, 2f, 1f, 0f, 1f, 0f, 1f)
        val calculated = FastFourierTransform.fft(data)
        val expected = listOf(
            ComplexNumber(6f, 0f),
            ComplexNumber(0f, -2f),
            ComplexNumber(-2f, 0f),
            ComplexNumber(0f, 2f),
            ComplexNumber(-2f, 0f),
            ComplexNumber(0f, -2f),
            ComplexNumber(-2f, 0f),
            ComplexNumber(0f, 2f)
        )

        assertEquals(expected.size, calculated.size)
        for (i in expected.indices) {
            complexEquals(expected[i], calculated[i], 0.001f)
        }
    }

    private fun complexEquals(expected: ComplexNumber, actual: ComplexNumber, tolerance: Float = 0.0001f) {
        assertEquals(expected.real, actual.real, tolerance)
        assertEquals(expected.imaginary, actual.imaginary, tolerance)
    }

}