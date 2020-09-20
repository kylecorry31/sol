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
    fun convertTemperature(fromTemp: Float, fromUnits: TemperatureUnits, toUnits: TemperatureUnits, expected: Float){
        val temp = unitService.convert(fromTemp, fromUnits, toUnits)
        assertEquals(expected, temp, 0.01f)
    }

    @ParameterizedTest
    @MethodSource("providePressures")
    fun convertPressure(pressure: Float, from: PressureUnits, to: PressureUnits, expected: Float){
        val p = unitService.convert(pressure, from, to)
        assertEquals(expected, p, 0.01f)
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
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Hpa, 1000f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Psi, 14.50377f),
                Arguments.of(14.50377f, PressureUnits.Psi, PressureUnits.Hpa, 1000f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Mbar, 1000f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Hpa, 1000f),
                Arguments.of(1000f, PressureUnits.Hpa, PressureUnits.Hpa, 1000f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.Inhg, 29.53f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Psi, 14.5037f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Inhg, 29.53f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Mbar, 1000f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Psi, 14.5037f),
                Arguments.of(14.50377f, PressureUnits.Psi, PressureUnits.Mbar, 1000f),
                Arguments.of(1000f, PressureUnits.Mbar, PressureUnits.Mbar, 1000f),
                Arguments.of(14.5037f, PressureUnits.Psi, PressureUnits.Psi, 14.5037f),
                Arguments.of(29.53f, PressureUnits.Inhg, PressureUnits.Inhg, 29.53f),
                )
        }
    }

}