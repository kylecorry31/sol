package com.kylecorry.trailsensecore.domain.weather

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import java.util.stream.Stream

internal class PressureReadingTest {

    @ParameterizedTest
    @MethodSource("provideHighPressures")
    fun isHigh(pressure: Float, isHigh: Boolean) {
        val reading = PressureReading(Instant.now(), pressure)
        assertEquals(isHigh, reading.isHigh())
    }

    @ParameterizedTest
    @MethodSource("provideLowPressures")
    fun isLow(pressure: Float, isLow: Boolean) {
        val reading = PressureReading(Instant.now(), pressure)
        assertEquals(isLow, reading.isLow())
    }

    companion object {
        @JvmStatic
        fun provideLowPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, true),
                Arguments.of(1009.144f, true),
                Arguments.of(1009.145f, false),
                Arguments.of(1013, false),
                Arguments.of(1030f, false),
            )
        }

        @JvmStatic
        fun provideHighPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, false),
                Arguments.of(1013, false),
                Arguments.of(1022.688f, false),
                Arguments.of(1022.689f, true),
                Arguments.of(1030f, true),
            )
        }
    }
}