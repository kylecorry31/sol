package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class WeightTest {
    @ParameterizedTest
    @MethodSource("provideConversions")
    fun convertTo(weight: Float, weightUnits: WeightUnits, expected: Float, expectedUnits: WeightUnits) {
        val converted = Weight.from(weight, weightUnits).convertTo(expectedUnits)
        assertEquals(expectedUnits, converted.units)
        assertEquals(expected, converted.value, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideTimes")
    fun times(
        weight: Float,
        units: WeightUnits,
        amount: Number,
        expected: Float,
        expectedUnits: WeightUnits
    ) {
        val multiplied = Weight.from(weight, units) * amount
        assertEquals(expectedUnits, multiplied.units)
        assertEquals(expected, multiplied.value, 0.00001f)
    }

    @ParameterizedTest
    @MethodSource("provideAdd")
    fun add(
        weight: Float,
        units: WeightUnits,
        other: Float,
        otherUnits: WeightUnits,
        expected: Float,
        expectedUnits: WeightUnits
    ) {
        val added = Weight.from(weight, units) + Weight.from(other, otherUnits)
        assertEquals(expectedUnits, added.units)
        assertEquals(expected, added.value, 0.00001f)
    }

    companion object {

        @JvmStatic
        fun provideConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(2f, WeightUnits.Pounds, 0.907185f, WeightUnits.Kilograms),
                Arguments.of(3f, WeightUnits.Kilograms, 3000f, WeightUnits.Grams),
                Arguments.of(4f, WeightUnits.Ounces, 0.25f, WeightUnits.Pounds),
                Arguments.of(4f, WeightUnits.Grams, 0.141096f, WeightUnits.Ounces),
                Arguments.of(4f, WeightUnits.Milligrams, 0.004f, WeightUnits.Grams),
                Arguments.of(1f, WeightUnits.Grains, 64.79891f, WeightUnits.Milligrams),
                Arguments.of(1f, WeightUnits.Grams, 1000f, WeightUnits.Milligrams),
            )
        }

        @JvmStatic
        fun provideTimes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(2f, WeightUnits.Pounds, 2, 4f, WeightUnits.Pounds),
                Arguments.of(3f, WeightUnits.Kilograms, 0.5f, 1.5f, WeightUnits.Kilograms),
                Arguments.of(4f, WeightUnits.Ounces, -3, 12f, WeightUnits.Ounces)
            )
        }

        @JvmStatic
        fun provideAdd(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(2f, WeightUnits.Pounds, 2f, WeightUnits.Pounds, 4f, WeightUnits.Pounds),
                Arguments.of(3f, WeightUnits.Kilograms, 500f, WeightUnits.Grams, 3.5f, WeightUnits.Kilograms),
                Arguments.of(500f, WeightUnits.Grams, 3f, WeightUnits.Kilograms, 3500f, WeightUnits.Grams),
            )
        }

    }
}