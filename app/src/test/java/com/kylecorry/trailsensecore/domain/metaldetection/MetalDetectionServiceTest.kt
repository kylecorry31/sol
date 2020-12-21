package com.kylecorry.trailsensecore.domain.metaldetection

import com.kylecorry.trailsensecore.domain.math.Vector3

import org.junit.Assert.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.sqrt

class MetalDetectionServiceTest {

    @ParameterizedTest
    @MethodSource("provideIsMetal")
    fun isMetal(field: Vector3, threshold: Float, expected: Boolean) {
        val service = MetalDetectionService()
        assertEquals(expected, service.isMetal(field, threshold))
    }

    @ParameterizedTest
    @MethodSource("provideFieldStrength")
    fun getFieldStrength(field: Vector3, expected: Float) {
        val service = MetalDetectionService()
        assertEquals(expected, service.getFieldStrength(field), 0.001f)
    }

    companion object {
        @JvmStatic
        fun provideIsMetal(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector3(0f, 1f, 0f), 0f, true),
                Arguments.of(Vector3(0f, 1f, 0f), 1f, true),
                Arguments.of(Vector3(0f, 1f, 0f), 2f, false),
            )
        }

        @JvmStatic
        fun provideFieldStrength(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector3(0f, 1f, 0f), 1f),
                Arguments.of(Vector3(0f, 0f, 0f), 0f),
                Arguments.of(Vector3(1f, 1f, 1f), sqrt(3f)),
            )
        }
    }
}