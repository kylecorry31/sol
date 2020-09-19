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
    }

}