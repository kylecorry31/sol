package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class Vector2Test {

    @ParameterizedTest
    @MethodSource("provideRotation")
    fun testRotation(vector: Vector2, angle: Float, origin: Vector2, expected: Vector2) {
        val rotated = vector.rotate(angle, origin)

        assertEquals(expected.x, rotated.x, 0.0001f)
        assertEquals(expected.y, rotated.y, 0.0001f)
    }

    companion object {
        @JvmStatic
        fun provideRotation(): Stream<Arguments> {
            return Stream.of(
                // CW
                Arguments.of(Vector2(1f, 0f), 90f, Vector2.zero, Vector2(0f, 1f)),
                Arguments.of(Vector2(1f, 0f), 180f, Vector2.zero, Vector2(-1f, 0f)),
                Arguments.of(Vector2(1f, 0f), 270f, Vector2.zero, Vector2(0f, -1f)),
                Arguments.of(Vector2(1f, 0f), 360f, Vector2.zero, Vector2(1f, 0f)),
                Arguments.of(Vector2(2f, 0f), 90f, Vector2(1f, 2f), Vector2(3f, 3f)),
                Arguments.of(Vector2(2f, 0f), 180f, Vector2(1f, 2f), Vector2(0f, 4f)),
                Arguments.of(Vector2(2f, 0f), 270f, Vector2(1f, 2f), Vector2(-1f, 1f)),
                Arguments.of(Vector2(2f, 0f), 360f, Vector2(1f, 2f), Vector2(2f, 0f)),
                // CCW
                Arguments.of(Vector2(1f, 0f), -90f, Vector2.zero, Vector2(0f, -1f)),
                Arguments.of(Vector2(1f, 0f), -180f, Vector2.zero, Vector2(-1f, 0f)),
                Arguments.of(Vector2(1f, 0f), -270f, Vector2.zero, Vector2(0f, 1f)),
                Arguments.of(Vector2(1f, 0f), -360f, Vector2.zero, Vector2(1f, 0f)),
                Arguments.of(Vector2(2f, 0f), -90f, Vector2(1f, 2f), Vector2(-1f, 1f)),
                Arguments.of(Vector2(2f, 0f), -180f, Vector2(1f, 2f), Vector2(0f, 4f)),
                Arguments.of(Vector2(2f, 0f), -270f, Vector2(1f, 2f), Vector2(3f, 3f)),
                Arguments.of(Vector2(2f, 0f), -360f, Vector2(1f, 2f), Vector2(2f, 0f)),
                // No rotation
                Arguments.of(Vector2(1f, 0f), 0f, Vector2.zero, Vector2(1f, 0f)),
                Arguments.of(Vector2(1f, 0f), 0f, Vector2(1f, 0f), Vector2(1f, 0f)),
            )
        }
    }

}