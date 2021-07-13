package com.kylecorry.trailsensecore.domain.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class WeightTest {
    @ParameterizedTest
    @MethodSource("provideConversions")
    fun convertTo(weight: Weight, expected: Weight) {
        val converted = weight.convertTo(expected.units)
        assertEquals(expected.units, converted.units)
        assertEquals(expected.weight, converted.weight, 0.0001f)
    }

    companion object {

        @JvmStatic
        fun provideConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Weight(2f, WeightUnits.Pounds), Weight(0.907185f, WeightUnits.Kilograms)),
                Arguments.of(Weight(3f, WeightUnits.Kilograms), Weight(3000f, WeightUnits.Grams)),
                Arguments.of(Weight(4f, WeightUnits.Ounces), Weight(0.25f, WeightUnits.Pounds)),
                Arguments.of(Weight(4f, WeightUnits.Grams), Weight(0.141096f, WeightUnits.Ounces)),
            )
        }

    }
}