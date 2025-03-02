package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class WeightTest {
    @ParameterizedTest
    @MethodSource("provideConversions")
    fun convertTo(weight: Quantity<Weight>, expected: Quantity<Weight>) {
        val converted = weight.convertTo(expected.units)
        assertEquals(expected.units, converted.units)
        assertEquals(expected.amount, converted.amount, 0.0001f)
    }

    companion object {

        @JvmStatic
        fun provideConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Quantity(2f, Weight.Pounds), Quantity(0.907185f, Weight.Kilograms)),
                Arguments.of(Quantity(3f, Weight.Kilograms), Quantity(3000f, Weight.Grams)),
                Arguments.of(Quantity(4f, Weight.Ounces), Quantity(0.25f, Weight.Pounds)),
                Arguments.of(Quantity(4f, Weight.Grams), Quantity(0.141096f, Weight.Ounces)),
            )
        }
    }
}