package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

class PhysicsServiceTest {

    @ParameterizedTest
    @MethodSource("provideFallHeights")
    fun fallHeight(time: Duration, height: Quantity<Distance>) {
        val service = PhysicsService()
        val actualHeight = service.fallHeight(time)
        assertEquals(actualHeight, height)
    }

    @ParameterizedTest
    @MethodSource("provideIsMetal")
    fun isMetal(field: Vector3, threshold: Float, expected: Boolean) {
        val service = PhysicsService()
        assertEquals(expected, service.isMetal(field, threshold))
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
        fun provideFallHeights(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Duration.ZERO, Quantity(0f, Distance.Meters)),
                Arguments.of(Duration.ofSeconds(1), Quantity(4.905f, Distance.Meters)),
                Arguments.of(Duration.ofSeconds(2), Quantity(19.62f, Distance.Meters)),
            )
        }
    }

}