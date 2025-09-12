package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class SpeedTest {

    @ParameterizedTest
    @MethodSource("provideSpeedConversions")
    fun convertTo(
        speed: Float,
        distanceUnits: DistanceUnits,
        timeUnits: TimeUnits,
        expected: Float,
        expectedDistanceUnits: DistanceUnits,
        expectedTimeUnits: TimeUnits
    ) {
        assertEquals(
            Speed.from(expected, expectedDistanceUnits, expectedTimeUnits),
            Speed.from(speed, distanceUnits, timeUnits).convertTo(expectedDistanceUnits, expectedTimeUnits)
        )
    }

    companion object {

        @JvmStatic
        fun provideSpeedConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1f, DistanceUnits.Meters, TimeUnits.Seconds, 1f, DistanceUnits.Meters, TimeUnits.Seconds),
                Arguments.of(
                    1f,
                    DistanceUnits.Meters,
                    TimeUnits.Seconds,
                    0.001f,
                    DistanceUnits.Meters,
                    TimeUnits.Milliseconds
                ),
                Arguments.of(1f, DistanceUnits.Meters, TimeUnits.Seconds, 60f, DistanceUnits.Meters, TimeUnits.Minutes),
                Arguments.of(1f, DistanceUnits.Meters, TimeUnits.Seconds, 3600f, DistanceUnits.Meters, TimeUnits.Hours),
                Arguments.of(1f, DistanceUnits.Meters, TimeUnits.Seconds, 86400f, DistanceUnits.Meters, TimeUnits.Days),
                Arguments.of(
                    5f,
                    DistanceUnits.Feet,
                    TimeUnits.Hours,
                    3657.6f,
                    DistanceUnits.Centimeters,
                    TimeUnits.Days
                ),
                Arguments.of(
                    86400f,
                    DistanceUnits.Centimeters,
                    TimeUnits.Days,
                    0.01f,
                    DistanceUnits.Meters,
                    TimeUnits.Seconds
                )
            )
        }

    }

}