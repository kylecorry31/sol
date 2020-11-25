package com.kylecorry.trailsensecore.domain.water

import org.junit.Assert.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

internal class WaterServiceTest {

    @ParameterizedTest
    @MethodSource("providePurificationTimes")
    fun getPurificationTime(altitude: Float?, duration: Duration) {
        val service = WaterService()
        val time = service.getPurificationTime(altitude)
        assertEquals(time, duration)
    }

    companion object {
        @JvmStatic
        fun providePurificationTimes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, Duration.ofMinutes(1)),
                Arguments.of(999f, Duration.ofMinutes(1)),
                Arguments.of(1000f, Duration.ofMinutes(3)),
                Arguments.of(2000f, Duration.ofMinutes(3)),
                Arguments.of(null, Duration.ofMinutes(3)),
            )
        }
    }
}