package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ExtensionsTest {

    @ParameterizedTest
    @MethodSource("provideFloatSums")
    fun sumOfFloat(values: List<Float>, sum: Float) {
        assertEquals(sum, values.sumOfFloat { it })
    }

    @ParameterizedTest
    @MethodSource("provideBatch")
    fun batch(values: List<Int>, n: Int, expected: List<List<Int>>) {
        val batched = values.batch(n)
        assertEquals(expected, batched)
    }

    @ParameterizedTest
    @MethodSource("provideSplit")
    fun split(values: List<Int>, percent: Float, expected: Pair<List<Int>, List<Int>>) {
        val split = values.split(percent)
        assertEquals(expected, split)
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

        @JvmStatic
        fun provideBatch(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Int>(), 2, listOf<List<Int>>()),
                Arguments.of(listOf(1), 2, listOf(listOf(1))),
                Arguments.of(listOf(1, 2), 2, listOf(listOf(1, 2))),
                Arguments.of(listOf(1, 2, 3), 2, listOf(listOf(1, 2), listOf(3))),
                Arguments.of(listOf(1, 2, 3, 4), 2, listOf(listOf(1, 2), listOf(3, 4))),
                Arguments.of(listOf(1, 2, 3, 4), 3, listOf(listOf(1, 2, 3), listOf(4))),
            )
        }

        @JvmStatic
        fun provideSplit(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf<Int>(), 0.5f, listOf<Int>() to listOf<Int>()),
                Arguments.of(listOf(1), 0.5f, listOf(1) to listOf<Int>()),
                Arguments.of(listOf(1, 2), 0.5f, listOf(1) to listOf(2)),
                Arguments.of(listOf(1, 2, 3), 0.5f, listOf(1, 2) to listOf(3)),
                Arguments.of(listOf(1, 2, 3), 0.333f, listOf(1) to listOf(2, 3)),
                Arguments.of(listOf(1, 2, 3), 0.666f, listOf(1, 2) to listOf(3)),
                Arguments.of(listOf(1, 2, 3), 0.75f, listOf(1, 2, 3) to listOf<Int>()),
                Arguments.of(listOf(1, 2, 3), 1f, listOf(1, 2, 3) to listOf<Int>()),
            )
        }

    }

}