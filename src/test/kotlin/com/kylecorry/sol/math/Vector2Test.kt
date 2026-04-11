package com.kylecorry.sol.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class Vector2Test {

    @ParameterizedTest
    @MethodSource("provideRotation")
    fun rotate(
        @ConvertWith(Vector2LongConverter::class) vector: Vector2,
        angle: Float,
        @ConvertWith(Vector2LongConverter::class) origin: Vector2,
        @ConvertWith(Vector2LongConverter::class) expected: Vector2
    ) {
        val rotated = vector.rotate(angle, origin)

        assertEquals(expected.x, rotated.x, 0.0001f)
        assertEquals(expected.y, rotated.y, 0.0001f)
    }

    // Rotate default (origin)
    @ParameterizedTest
    @CsvSource(
        "1, 0, 90, 0, 1",
        "1, 0, 180, -1, 0",
        "1, 0, 270, 0, -1",
        "1, 0, 360, 1, 0",
    )
    fun rotateDefault(x: Float, y: Float, angle: Float, expectedX: Float, expectedY: Float) {
        val vector = Vector2(x, y)
        val rotated = vector.rotate(angle)

        assertEquals(expectedX, rotated.x, 0.0001f)
        assertEquals(expectedY, rotated.y, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0",
        "1, 0, 0",
        "0, 1, 90",
        "-1, 0, 180",
        "0, -1, 270",
        "1, 1, 45",
        "-1, 1, 135",
        "-1, -1, 225",
        "1, -1, 315"
    )
    fun angle(x: Float, y: Float, expected: Float) {
        val vector = Vector2(x, y)
        assertEquals(expected, vector.angle(), 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0, 0",
        "1, 0, 0, 0, 1, 0",
        "0, 1, 0, 0, 0, 1",
        "1, 1, 0, 0, 1, 1",
        "1, 1, 1, 1, 2, 2",
        "1, 1, -1, -1, 0, 0",
        "1, 1, 1, -1, 2, 0",
        "1, 1, -1, 1, 0, 2"
    )
    fun plus(x: Float, y: Float, otherX: Float, otherY: Float, expectedX: Float, expectedY: Float) {
        val vector = Vector2(x, y)
        val other = Vector2(otherX, otherY)
        val result = vector + other
        assertEquals(expectedX, result.x, 0.0001f)
        assertEquals(expectedY, result.y, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0, 0",
        "1, 0, 0, 0, 1, 0",
        "0, 1, 0, 0, 0, 1",
        "1, 1, 0, 0, 1, 1",
        "1, 1, 1, 1, 0, 0",
        "1, 1, -1, -1, 2, 2",
        "1, 1, 1, -1, 0, 2",
        "1, 1, -1, 1, 2, 0"
    )
    fun minus(x: Float, y: Float, otherX: Float, otherY: Float, expectedX: Float, expectedY: Float) {
        val vector = Vector2(x, y)
        val other = Vector2(otherX, otherY)
        val result = vector - other
        assertEquals(expectedX, result.x, 0.0001f)
        assertEquals(expectedY, result.y, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0",
        "1, 0, 0, 0, 0",
        "0, 1, 0, 0, 0",
        "1, 0, 1, 1, 0",
        "0, 1, 1, 0, 1",
        "1, 2, 3, 3, 6",
        "1, 2, -1, -1, -2"
    )
    fun times(x: Float, y: Float, scale: Float, expectedX: Float, expectedY: Float) {
        val vector = Vector2(x, y)
        val result = vector * scale
        assertEquals(expectedX, result.x, 0.0001f)
        assertEquals(expectedY, result.y, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0",
        "1, 0, 1",
        "0, 1, 1",
        "1, 1, 1.4142135"
    )
    fun magnitude(x: Float, y: Float, expected: Float) {
        val vector = Vector2(x, y)
        assertEquals(expected, vector.magnitude(), 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0",
        "1, 0, 0, 0, 1",
        "0, 1, 0, 0, 1",
        "1, 1, 0, 0, 1.4142135",
        "1, 1, 0, 1, 1",
    )
    fun distanceTo(x: Float, y: Float, otherX: Float, otherY: Float, expected: Float) {
        val vector = Vector2(x, y)
        val other = Vector2(otherX, otherY)
        assertEquals(expected, vector.distanceTo(other), 0.0001f)
    }

    // Normalize
    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0",
        "1, 0, 1, 0",
        "0, 1, 0, 1",
        "1, 1, 0.70710677, 0.70710677",
        "1, 2, 0.4472136, 0.8944272",
    )
    fun normalize(x: Float, y: Float, expectedX: Float, expectedY: Float) {
        val vector = Vector2(x, y)
        val normalized = vector.normalize()
        assertEquals(expectedX, normalized.x, 0.0001f)
        assertEquals(expectedY, normalized.y, 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0",
        "1, 0, 1",
        "0, 1, 1",
        "1, 1, 2",
    )
    fun squaredMagnitude(x: Float, y: Float, expected: Float) {
        val vector = Vector2(x, y)
        assertEquals(expected, vector.squaredMagnitude(), 0.0001f)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0",
        "1, 0, 0, 0, 1",
        "0, 1, 0, 0, 1",
        "1, 1, 0, 0, 2",
        "1, 1, 0, 1, 1",
    )
    fun squaredDistanceTo(x: Float, y: Float, otherX: Float, otherY: Float, expected: Float) {
        val vector = Vector2(x, y)
        val other = Vector2(otherX, otherY)
        assertEquals(expected, vector.squaredDistanceTo(other), 0.0001f)
    }

    // Angle between
    @ParameterizedTest
    @CsvSource(
        "0, 0, 0, 0, 0",
        "1, 0, 0, 0, 180",
        "0, 1, 0, 0, 270",
        "1, 1, 0, 0, 225",
        "1, 1, 1, 1, 0",
        "1, 1, -1, -1, 225",
        "1, 1, 1, -1, 270",
        "1, 1, -1, 1, 180",
    )
    fun angleBetween(x: Float, y: Float, otherX: Float, otherY: Float, expected: Float) {
        val vector = Vector2(x, y)
        val other = Vector2(otherX, otherY)
        assertEquals(expected, vector.angleBetween(other), 0.0001f)
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