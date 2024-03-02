package com.kylecorry.sol.shared

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TextExtensionsKtTest {

    @Test
    fun toDoubleCompat() {
        assertEquals(1.0, "1".toDoubleCompat())
        assertEquals(1.0, "1.0".toDoubleCompat())
        assertEquals(1.0, "1,0".toDoubleCompat())
        assertEquals(null, "ab".toDoubleCompat())
        assertEquals(null, "NaN".toDoubleCompat())
        assertEquals(null, "Infinity".toDoubleCompat())
    }

    @Test
    fun toFloatCompat() {
        assertEquals(1.0f, "1".toFloatCompat())
        assertEquals(1.0f, "1.0".toFloatCompat())
        assertEquals(1.0f, "1,0".toFloatCompat())
        assertEquals(null, "ab".toFloatCompat())
        assertEquals(null, "NaN".toFloatCompat())
        assertEquals(null, "Infinity".toFloatCompat())
    }
}