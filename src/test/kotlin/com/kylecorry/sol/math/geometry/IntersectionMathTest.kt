package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector2LongConverter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class IntersectionMathTest {

    @ParameterizedTest
    @MethodSource("provideLineRectIntersection")
    fun getIntersectionLineRect(line: Line, rect: Rectangle, expected: List<Vector2>) {
        val intersection = IntersectionMath.getIntersection(line, rect)
        assertEquals(expected.size, intersection.size)
        expected.forEach {
            assertTrue(intersection.contains(it))
        }
    }

    @ParameterizedTest
    @MethodSource("providePointsRectIntersection")
    fun getIntersectionPointsRect(
        @ConvertWith(Vector2LongConverter::class) a: Vector2,
        @ConvertWith(Vector2LongConverter::class) b: Vector2,
        rect: Rectangle,
        @ConvertWith(Vector2LongConverter::class) expected: List<Vector2>
    ) {
        val intersection = IntersectionMath.getIntersection(a, b, rect)
        assertEquals(expected.size, intersection.size)
        expected.forEach {
            assertTrue(intersection.contains(it))
        }
    }

    companion object {
        @JvmStatic
        fun provideLineRectIntersection(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(10f, 10f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(10f, 10f))
                ),
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(10f, 10f)),
                    Rectangle(0f, 5f, 5f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(5f, 5f))
                ),
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(10f, 10f)),
                    Rectangle(5f, 10f, 10f, 5f),
                    listOf(Vector2(5f, 5f), Vector2(10f, 10f))
                ),
                // Horizontal line
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(10f, 0f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(10f, 0f))
                ),
                // Vertical line
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(0f, 10f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(0f, 10f))
                ),
                // Diagonal, no intersection
                Arguments.of(
                    Line(Vector2(-10f, 0f), Vector2(-1f, 10f)),
                    Rectangle(0f, 5f, 5f, 0f),
                    emptyList<Vector2>()
                ),
                // Horizontal, no intersection
                Arguments.of(
                    Line(Vector2(0f, -1f), Vector2(10f, -1f)),
                    Rectangle(0f, 5f, 5f, 0f),
                    emptyList<Vector2>()
                ),
                // Vertical, no intersection
                Arguments.of(
                    Line(Vector2(-1f, 0f), Vector2(-1f, 10f)),
                    Rectangle(0f, 5f, 5f, 0f),
                    emptyList<Vector2>()
                ),
                // Horizontal, same as top
                Arguments.of(
                    Line(Vector2(0f, 10f), Vector2(10f, 10f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 10f), Vector2(10f, 10f))
                ),
                // Horizontal, same as bottom
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(10f, 0f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(10f, 0f))
                ),
                // Vertical, same as left
                Arguments.of(
                    Line(Vector2(0f, 0f), Vector2(0f, 10f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(0f, 10f))
                ),
                // Vertical, same as right
                Arguments.of(
                    Line(Vector2(10f, 0f), Vector2(10f, 10f)),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(10f, 0f), Vector2(10f, 10f))
                ),
            )
        }

        @JvmStatic
        fun providePointsRectIntersection(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(10f, 10f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(10f, 10f))
                ),
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(10f, 10f),
                    Rectangle(0f, 5f, 5f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(5f, 5f))
                ),
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(10f, 10f),
                    Rectangle(5f, 10f, 10f, 5f),
                    listOf(Vector2(5f, 5f), Vector2(10f, 10f))
                ),
                // Horizontal line
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(10f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(10f, 0f))
                ),
                // Vertical line
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(0f, 10f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(0f, 10f))
                ),
                // Diagonal, no intersection
                Arguments.of(
                    Vector2(-10f, 0f),
                    Vector2(-1f, 10f),
                    Rectangle(0f, 5f, 5f, 0f),
                    emptyList<Vector2>()
                ),
                // Horizontal, no intersection
                Arguments.of(
                    Vector2(0f, -1f),
                    Vector2(10f, -1f),
                    Rectangle(0f, 5f, 5f, 0f),
                    emptyList<Vector2>()
                ),
                // Vertical, no intersection
                Arguments.of(
                    Vector2(-1f, 0f),
                    Vector2(-1f, 10f),
                    Rectangle(0f, 5f, 5f, 0f),
                    emptyList<Vector2>()
                ),
                // Horizontal, same as top
                Arguments.of(
                    Vector2(0f, 10f),
                    Vector2(10f, 10f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 10f), Vector2(10f, 10f))
                ),
                // Horizontal, same as bottom
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(10f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(10f, 0f))
                ),
                // Vertical, same as left
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(0f, 10f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f), Vector2(0f, 10f))
                ),
                // Vertical, same as right
                Arguments.of(
                    Vector2(10f, 0f),
                    Vector2(10f, 10f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(10f, 0f), Vector2(10f, 10f))
                ),
                // Ends before left of rectangle
                Arguments.of(
                    Vector2(-10f, 0f),
                    Vector2(-5f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    emptyList<Vector2>()
                ),
                // Starts after right of rectangle
                Arguments.of(
                    Vector2(15f, 0f),
                    Vector2(20f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    emptyList<Vector2>()
                ),
                // Starts above top of rectangle
                Arguments.of(
                    Vector2(0f, 15f),
                    Vector2(0f, 20f),
                    Rectangle(0f, 10f, 10f, 0f),
                    emptyList<Vector2>()
                ),
                // Starts below bottom of rectangle
                Arguments.of(
                    Vector2(0f, -10f),
                    Vector2(0f, -5f),
                    Rectangle(0f, 10f, 10f, 0f),
                    emptyList<Vector2>()
                ),
                // Starts before left of rectangle, ends inside rectangle
                Arguments.of(
                    Vector2(-10f, 0f),
                    Vector2(5f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f))
                ),
                // Starts in rectangle, ends after right of rectangle
                Arguments.of(
                    Vector2(5f, 0f),
                    Vector2(15f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(10f, 0f))
                ),
                // Starts above top of rectangle, ends inside rectangle
                Arguments.of(
                    Vector2(0f, 15f),
                    Vector2(0f, 5f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 10f))
                ),
                // Starts in rectangle, ends below bottom of rectangle
                Arguments.of(
                    Vector2(0f, 5f),
                    Vector2(0f, -5f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f))
                ),
                // Starts in rectangle, ends above top of rectangle
                Arguments.of(
                    Vector2(0f, 5f),
                    Vector2(0f, 15f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 10f))
                ),
                // Starts in rectangle, ends inside rectangle (point)
                Arguments.of(
                    Vector2(5f, 5f),
                    Vector2(5f, 5f),
                    Rectangle(0f, 10f, 10f, 0f),
                    emptyList<Vector2>()
                ),
                // Starts in rectangle, ends inside rectangle (line has distance)
                Arguments.of(
                    Vector2(5f, 5f),
                    Vector2(5f, 6f),
                    Rectangle(0f, 10f, 10f, 0f),
                    emptyList<Vector2>()
                ),
                // Single point on border
                Arguments.of(
                    Vector2(0f, 0f),
                    Vector2(0f, 0f),
                    Rectangle(0f, 10f, 10f, 0f),
                    listOf(Vector2(0f, 0f))
                ),
            )
        }
    }

}