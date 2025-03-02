package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest

import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class SpeedTest {

    @ParameterizedTest
    @MethodSource("provideSpeedConversions")
    fun convertTo(speed: Speed, expected: Speed) {
        assertEquals(expected, speed.convertTo(expected.distanceUnits, expected.timeUnits))
    }

    companion object {

        @JvmStatic
        fun provideSpeedConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Speed(1f, Distance.Meters, Time.Seconds), Speed(1f, Distance.Meters, Time.Seconds)),
                Arguments.of(Speed(1f, Distance.Meters, Time.Seconds), Speed(0.001f, Distance.Meters, Time.Milliseconds)),
                Arguments.of(Speed(1f, Distance.Meters, Time.Seconds), Speed(60f, Distance.Meters, Time.Minutes)),
                Arguments.of(Speed(1f, Distance.Meters, Time.Seconds), Speed(3600f, Distance.Meters, Time.Hours)),
                Arguments.of(Speed(1f, Distance.Meters, Time.Seconds), Speed(86400f, Distance.Meters, Time.Days)),
                Arguments.of(Speed(5f, Distance.Feet, Time.Hours), Speed(3657.6f, Distance.Centimeters, Time.Days)),
                Arguments.of(Speed(86400f, Distance.Centimeters, Time.Days), Speed(0.01f, Distance.Meters, Time.Seconds))
            )
        }

    }

}