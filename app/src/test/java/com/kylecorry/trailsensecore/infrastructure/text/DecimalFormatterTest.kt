package com.kylecorry.trailsensecore.infrastructure.text

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class DecimalFormatterTest {

    @ParameterizedTest
    @MethodSource("provideValues")
    fun format(value: Number, places: Int, strict: Boolean, expected: String) {
        assertEquals(expected, DecimalFormatter.format(value, places, strict))
    }

    companion object {
        @JvmStatic
        fun provideValues(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1.155, 1, false, "1.2"),
                Arguments.of(1.155, 1, true, "1.2"),
                Arguments.of(1.155, 4, false, "1.155"),
                Arguments.of(1.155, 4, true, "1.1550"),
                Arguments.of(1.155, 0, true, "1"),
                )
        }
    }

}