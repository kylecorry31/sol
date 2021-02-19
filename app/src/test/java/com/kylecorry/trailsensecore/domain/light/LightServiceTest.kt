package com.kylecorry.trailsensecore.domain.light

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.DistanceUnits
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class LightServiceTest {
    @ParameterizedTest
    @MethodSource("provideBeamDistance")
    fun beamDistance(candela: Float, distanceMeters: Float){
        val service = LightService()
        val beamDistance = service.beamDistance(candela)
        assertEquals(distanceMeters, beamDistance.distance, 0.5f)
        assertEquals(DistanceUnits.Meters, beamDistance.units)
    }

    @ParameterizedTest
    @MethodSource("provideLuxAtDistance")
    fun luxAtDistance(candela: Float, distance: Distance, lux: Float){
        val service = LightService()
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
    }

}