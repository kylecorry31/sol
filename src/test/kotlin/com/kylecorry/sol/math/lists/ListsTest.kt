package com.kylecorry.sol.math.lists

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ListsTest {

    @ParameterizedTest
    @MethodSource("provideArgmax")
    fun <T : Comparable<T>> argmax(value: List<T>, expected: Int) {
        val actual = Lists.argmax(value)
        assertEquals(expected, actual)
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