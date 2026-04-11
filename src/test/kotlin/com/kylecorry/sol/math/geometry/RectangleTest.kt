package com.kylecorry.sol.math.geometry

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class RectangleTest {

    @ParameterizedTest
    @MethodSource("provideIntersectingRectangles")
    fun intersects(rect1: Rectangle, rect2: Rectangle) {
        assertTrue(rect1.intersects(rect2))
        assertTrue(rect2.intersects(rect1))
    }

    @ParameterizedTest
    @MethodSource("provideNonIntersectingRectangles")
    fun doesNotIntersect(rect1: Rectangle, rect2: Rectangle) {
        assertFalse(rect1.intersects(rect2))
        assertFalse(rect2.intersects(rect1))
    }

    companion object {
        @JvmStatic
        fun provideIntersectingRectangles(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Rectangle(0f, 10f, 10f, 0f),
                    Rectangle(0f, 10f, 10f, 0f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 10f, 0f),
                    Rectangle(2f, 8f, 8f, 2f)
                ),
                Arguments.of(
                    Rectangle(2f, 8f, 8f, 2f),
                    Rectangle(0f, 10f, 10f, 0f)
                ),
                Arguments.of(
                    Rectangle(0f, 6f, 10f, 4f),
                    Rectangle(4f, 10f, 6f, 0f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 6f, 4f),
                    Rectangle(4f, 8f, 10f, 0f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 5f, 5f),
                    Rectangle(5f, 10f, 10f, 5f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 5f, 5f),
                    Rectangle(5f, 5f, 10f, 0f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 10f, 5f),
                    Rectangle(2f, 5f, 8f, 0f)
                )
            )
        }

        @JvmStatic
        fun provideNonIntersectingRectangles(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Rectangle(0f, 10f, 4f, 6f),
                    Rectangle(5f, 10f, 9f, 6f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 4f, 6f),
                    Rectangle(0f, 5f, 4f, 1f)
                ),
                Arguments.of(
                    Rectangle(0f, 10f, 4f, 6f),
                    Rectangle(5f, 5f, 9f, 1f)
                )
            )
        }
    }
}
