package com.kylecorry.trailsensecore.meteorology

import com.kylecorry.trailsensecore.meteorology.PressureAltitudeReading
import com.kylecorry.trailsensecore.meteorology.PressureReading
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import java.util.stream.Stream

internal class PressureAltitudeReadingTest {

    @ParameterizedTest
    @MethodSource("provideSeaLevelPressure")
    fun convertsToSeaLevel(pressure: Float, altitude: Float, temperature: Float, useTemperature: Boolean, expected: Float){
        val reading = PressureAltitudeReading(Instant.now(), pressure, altitude, temperature)
        val sl = reading.seaLevel(useTemperature)
        assertEquals(expected, sl.value, 0.1f)
    }

    @Test
    fun defaultsToUsingTempInSeaLevel(){
        val reading = PressureAltitudeReading(Instant.now(), 980f, 1000f, 15f)
        val sl = reading.seaLevel()
        assertEquals(1101.93f, sl.value, 0.1f)
    }

    @Test
    fun convertsToPressureReading(){
        val i = Instant.ofEpochMilli(2000)
        val reading = PressureAltitudeReading(i, 1f, 2f, 3f)
        val pressure = reading.pressureReading()
        assertEquals(PressureReading(i, 1f), pressure)
    }


    companion object {
        @JvmStatic
        fun provideSeaLevelPressure(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, 0f, 0f, false, 0f),
                Arguments.of(0f, 0f, 0f, true, 0f),
                Arguments.of(1000f, -100f, 0f, false, 988.2f),
                Arguments.of(980f, 200f, 0f, false, 1003.48f),
                Arguments.of(980f, 1000f, 15f, true, 1101.93f),
                Arguments.of(1000f, -100f, 28f, true, 988.71f),
            )
        }
    }


}