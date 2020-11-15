package com.kylecorry.trailsensecore.domain.physics

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

class PhysicsServiceTest {

    @ParameterizedTest
    @MethodSource("provideFallHeights")
    fun fallHeight(time: Duration, height: Float) {
        val service = PhysicsService()
        val actualHeight = service.fallHeight(time)
        assertEquals(actualHeight, height, 0.001f)
    }


    companion object {
        @JvmStatic
        fun provideFallHeights(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Duration.ZERO, 0f),
                Arguments.of(Duration.ofSeconds(1), 4.905f),
                Arguments.of(Duration.ofSeconds(2), 19.62f),
            )
        }
    }

}