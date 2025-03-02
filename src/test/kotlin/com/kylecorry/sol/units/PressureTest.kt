package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class PressureTest {

    @ParameterizedTest
    @MethodSource("providePressures")
    fun convertPressure(pressure: Float, from: Pressure, to: Pressure, expected: Float) {
        val p = Quantity(pressure, from).convertTo(to)
        assertEquals(expected, p.amount, 0.01f)
        assertEquals(to, p.units)
    }

    companion object {
        @JvmStatic
        fun providePressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, Pressure.Hpa, Pressure.Inhg, 29.53f),
                Arguments.of(1000f, Pressure.Hpa, Pressure.Psi, 14.50377f),
                Arguments.of(1000f, Pressure.Hpa, Pressure.Mbar, 1000f),
                Arguments.of(1000f, Pressure.Hpa, Pressure.Hpa, 1000f),
                Arguments.of(1000f, Pressure.Hpa, Pressure.MmHg, 750.06158f),

                Arguments.of(1000f, Pressure.Mbar, Pressure.Hpa, 1000f),
                Arguments.of(1000f, Pressure.Mbar, Pressure.Inhg, 29.53f),
                Arguments.of(1000f, Pressure.Mbar, Pressure.Psi, 14.5037f),
                Arguments.of(1000f, Pressure.Mbar, Pressure.Mbar, 1000f),
                Arguments.of(1000f, Pressure.Mbar, Pressure.MmHg, 750.06158f),

                Arguments.of(29.53f, Pressure.Inhg, Pressure.Hpa, 1000f),
                Arguments.of(29.53f, Pressure.Inhg, Pressure.Psi, 14.5037f),
                Arguments.of(29.53f, Pressure.Inhg, Pressure.Mbar, 1000f),
                Arguments.of(29.53f, Pressure.Inhg, Pressure.Inhg, 29.53f),
                Arguments.of(29.53f, Pressure.Inhg, Pressure.MmHg, 750.06158f),

                Arguments.of(14.50377f, Pressure.Psi, Pressure.Hpa, 1000f),
                Arguments.of(14.5037f, Pressure.Psi, Pressure.Inhg, 29.53f),
                Arguments.of(14.50377f, Pressure.Psi, Pressure.Mbar, 1000f),
                Arguments.of(14.5037f, Pressure.Psi, Pressure.Psi, 14.5037f),
                Arguments.of(14.5037f, Pressure.Psi, Pressure.MmHg, 750.06158f),

                Arguments.of(750.06158f, Pressure.MmHg, Pressure.Hpa, 1000f),
                Arguments.of(750.06158f, Pressure.MmHg, Pressure.Inhg, 29.53f),
                Arguments.of(750.06158f, Pressure.MmHg, Pressure.Mbar, 1000f),
                Arguments.of(750.06158f, Pressure.MmHg, Pressure.Psi, 14.5037f),
                Arguments.of(750.06158f, Pressure.MmHg, Pressure.MmHg, 750.06158f)
            )
        }
    }

}