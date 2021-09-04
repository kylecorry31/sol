package com.kylecorry.trailsensecore.science.physics

import com.kylecorry.andromeda.core.math.Vector3
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import org.junit.Assert
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

    @ParameterizedTest
    @MethodSource("provideIsMetal")
    fun isMetal(field: Vector3, threshold: Float, expected: Boolean) {
        val service = PhysicsService()
        Assert.assertEquals(expected, service.isMetal(field, threshold))
    }

    @ParameterizedTest
    @MethodSource("provideBeamDistance")
    fun beamDistance(candela: Float, distanceMeters: Float) {
        val service = PhysicsService()
        val beamDistance = service.lightBeamDistance(candela)
        assertEquals(distanceMeters, beamDistance.distance, 0.5f)
        assertEquals(DistanceUnits.Meters, beamDistance.units)
    }

    @ParameterizedTest
    @MethodSource("provideLuxAtDistance")
    fun luxAtDistance(candela: Float, distance: Distance, lux: Float) {
        val service = PhysicsService()
        val actualLux = service.luxAtDistance(candela, distance)
        assertEquals(lux, actualLux, 0.1f)
    }

    companion object {
        @JvmStatic
        fun provideBeamDistance(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(8148f, 181f),
                Arguments.of(5600f, 150f)
            )
        }

        @JvmStatic
        fun provideLuxAtDistance(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(8148f, Distance.meters(181f), 0.25f),
                Arguments.of(5600f, Distance.meters(150f), 0.25f)
            )
        }

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
                Arguments.of(Duration.ZERO, Distance(0f, DistanceUnits.Meters)),
                Arguments.of(Duration.ofSeconds(1), Distance(4.905f, DistanceUnits.Meters)),
                Arguments.of(Duration.ofSeconds(2), Distance(19.62f, DistanceUnits.Meters)),
            )
        }
    }

}