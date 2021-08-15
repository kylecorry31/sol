package com.kylecorry.trailsensecore.domain.physics

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

class PhysicsServiceTest {

    @ParameterizedTest
    @MethodSource("provideFallHeights")
    fun fallHeight(time: Duration, height: Distance) {
        val service = PhysicsService()
        val actualHeight = service.fallHeight(time)
        assertEquals(actualHeight, height)
    }


    companion object {
        @JvmStatic
        fun provideFallHeights(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Duration.ZERO, Distance(0f, DistanceUnits.Meters)),
                Arguments.of(Duration.ofSeconds(1), Distance(4.905f, DistanceUnits.Meters)),
                Arguments.of(Duration.ofSeconds(2), Distance(19.62f, DistanceUnits.Meters)),
            )
        }
    }

}