package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ExtensionsTest {

    @ParameterizedTest
    @MethodSource("provideFloatSums")
    fun sumOfFloat(values: List<Float>, sum: Float){
        assertEquals(sum, values.sumOfFloat { it})
    }

    companion object {

        @JvmStatic
        fun provideFloatSums(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf(1f, 2f, 3f), 6f),
                Arguments.of(listOf(1f, 2f, 3f, 4f), 10f),
                Arguments.of(listOf(1f, 2f, 3f, 4f, 5f), 15f),
                Arguments.of(emptyList<Float>(), 0f),
                Arguments.of(listOf(1f), 1f),
            )
        }

    }

}