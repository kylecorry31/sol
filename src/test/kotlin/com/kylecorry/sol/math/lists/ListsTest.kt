package com.kylecorry.sol.math.lists

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ListsTest {

    @ParameterizedTest
    @MethodSource("provideArgmax")
    fun <T : Comparable<T>> argmax(value: List<T>, expected: Int) {
        val actual = Lists.argmax(value)
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0",
        "1, 0",
        "1.5, 1",
        "2, 1",
        "3, 4",
        "5, 4",
        "6, 5"
    )
    fun binarySearch(value: Double, expected: Int) {
        // The first of repeated values is found
        val sorted = listOf(1.0, 2.0, 2.0, 2.0, 5.0)
        assertEquals(expected, Lists.binarySearch(sorted) { it >= value })
    }

    companion object {
        @JvmStatic
        fun provideArgmax(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf(1f, 2f, 3f), 2),
                Arguments.of(listOf(1f, 0.5f, 0.75f), 0),
                Arguments.of(listOf(1f, 1f, 1f), 0),
                Arguments.of(listOf<Float>(), -1),
            )
        }
    }
}