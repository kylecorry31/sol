package com.kylecorry.trailsensecore.domain.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class UnitServiceTest {

    private val unitService = UnitService()


    @ParameterizedTest
    @MethodSource("provideTemperatures")
    fun convertTemperature(
        fromTemp: Float,
        fromUnits: TemperatureUnits,
        toUnits: TemperatureUnits,
        expected: Float
    ) {
        val temp = unitService.convert(fromTemp, fromUnits, toUnits)
        assertEquals(expected, temp, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("providePressures")
    fun convertPressure(pressure: Float, from: PressureUnits, to: PressureUnits, expected: Float) {
        val p = unitService.convert(pressure, from, to)
        assertEquals(expected, p, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("provideDistances")
    fun convertDistance(distance: Float, from: DistanceUnits, to: DistanceUnits, expected: Float) {
        val d = unitService.convert(distance, from, to)
        assertEquals(expected, d, 0.001f)
    }


    companion object {
        @JvmStatic
        fun provideTemperatures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, TemperatureUnits.C, TemperatureUnits.F, 32f),
                Arguments.of(1f, TemperatureUnits.C, TemperatureUnits.C, 1f),
                Arguments.of(2f, TemperatureUnits.F, TemperatureUnits.F, 2f),
                Arguments.of(32f, TemperatureUnits.F, TemperatureUnits.C, 0f),
                Arguments.of(16f, TemperatureUnits.F, TemperatureUnits.C, -8.889f),
                Arguments.of(-8.889f, TemperatureUnits.C, TemperatureUnits.F, 16f),
            )
        }

        @JvmStatic
        fun providePressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Inhg, 29.53f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Psi, 14.50377f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Mbar, 1000f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Hpa, 1000f),

                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Hpa, 1000f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Inhg, 29.53f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Psi, 14.5037f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Mbar, 1000f),

                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Hpa, 1000f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Psi, 14.5037f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Mbar, 1000f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Inhg, 29.53f),

                Arguments.of(14.50377f, PressureUnits.Psi, PressureUnits.Hpa, 1000f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.Inhg, 29.53f),
                Arguments.of(14.50377f, PressureUnits.Psi, PressureUnits.Mbar, 1000f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.Psi, 14.5037f)
            )
        }

        @JvmStatic
        fun provideDistances(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1500f, DistanceUnits.Meters, DistanceUnits.Meters, 1500f),
                Arguments.of(1500f, DistanceUnits.Meters, DistanceUnits.Feet, 4921.26f),
                Arguments.of(1500f, DistanceUnits.Meters, DistanceUnits.Kilometers, 1.5f),
                Arguments.of(1500f, DistanceUnits.Meters, DistanceUnits.Miles, 0.9320568f),
                Arguments.of(1500f, DistanceUnits.Meters, DistanceUnits.NauticalMiles, 0.8099352f),

                Arguments.of(300f, DistanceUnits.Feet, DistanceUnits.Meters, 91.44f),
                Arguments.of(300f, DistanceUnits.Feet, DistanceUnits.Feet, 300f),
                Arguments.of(300f, DistanceUnits.Feet, DistanceUnits.Kilometers, 0.09144f),
                Arguments.of(300f, DistanceUnits.Feet, DistanceUnits.Miles, 0.0568182f),
                Arguments.of(300f, DistanceUnits.Feet, DistanceUnits.NauticalMiles, 0.0493737f),

                Arguments.of(1.5f, DistanceUnits.Kilometers, DistanceUnits.Meters, 1500f),
                Arguments.of(1.5f, DistanceUnits.Kilometers, DistanceUnits.Feet, 4921.26f),
                Arguments.of(1.5f, DistanceUnits.Kilometers, DistanceUnits.Kilometers, 1.5f),
                Arguments.of(1.5f, DistanceUnits.Kilometers, DistanceUnits.Miles, 0.9320568f),
                Arguments.of(
                    1.5f,
                    DistanceUnits.Kilometers,
                    DistanceUnits.NauticalMiles,
                    0.809935f
                ),

                Arguments.of(1f, DistanceUnits.Miles, DistanceUnits.Meters, 1609.344f),
                Arguments.of(1f, DistanceUnits.Miles, DistanceUnits.Feet, 5280f),
                Arguments.of(1f, DistanceUnits.Miles, DistanceUnits.Kilometers, 1.609344f),
                Arguments.of(1f, DistanceUnits.Miles, DistanceUnits.Miles, 1f),
                Arguments.of(1f, DistanceUnits.Miles, DistanceUnits.NauticalMiles, 0.868976f),

                Arguments.of(1f, DistanceUnits.NauticalMiles, DistanceUnits.Meters, 1852f),
                Arguments.of(1f, DistanceUnits.NauticalMiles, DistanceUnits.Feet, 6076.1155337f),
                Arguments.of(1f, DistanceUnits.NauticalMiles, DistanceUnits.Kilometers, 1.852f),
                Arguments.of(1f, DistanceUnits.NauticalMiles, DistanceUnits.Miles, 1.15078f),
                Arguments.of(1f, DistanceUnits.NauticalMiles, DistanceUnits.NauticalMiles, 1f)
            )
        }

    }

}