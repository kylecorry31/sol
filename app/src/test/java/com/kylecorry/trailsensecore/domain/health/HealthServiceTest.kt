package com.kylecorry.trailsensecore.domain.health

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

internal class HealthServiceTest {

    @ParameterizedTest
    @MethodSource("getHeartRates")
    fun getHeartRate(beats: Int, duration: Duration, bpm: Float) {
        val service = HealthService()
        assertEquals(bpm, service.getHeartRate(beats, duration), 0.1f)
    }

    companion object {

        @JvmStatic
        fun getHeartRates(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(30, Duration.ofSeconds(30), 60f),
                Arguments.of(60, Duration.ofSeconds(30), 120f),
                Arguments.of(5, Duration.ofSeconds(10), 30f),
                Arguments.of(45, Duration.ofMinutes(1), 45f),
                Arguments.of(5, Duration.ofSeconds(0), 0f),
            )
        }
    }
}