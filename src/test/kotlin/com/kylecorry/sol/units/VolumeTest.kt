package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class VolumeTest {
    @ParameterizedTest
    @MethodSource("provideConversions")
    fun convertTo(volume: Quantity<Volume>, expected: Quantity<Volume>) {
        val converted = volume.convertTo(expected.units)
        assertEquals(expected.units, converted.units)
        assertEquals(expected.amount, converted.amount, 0.0001f)
    }

    companion object {

        @JvmStatic
        fun provideConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quantity(2f, Volume.Liters),
                    Quantity(0.528344f, Volume.USGallons)
                ),
                Arguments.of(
                    Quantity(3f, Volume.USGallons),
                    Quantity(11.3562f, Volume.Liters)
                ),
                Arguments.of(
                    Quantity(3f, Volume.ImperialGallons),
                    Quantity(461.16525f, Volume.USOunces)
                ),
                Arguments.of(
                    Quantity(4f, Volume.Milliliter),
                    Quantity(0.014078f, Volume.ImperialCups)
                ),
                Arguments.of(Quantity(4f, Volume.USPints), Quantity(2f, Volume.USQuarts)),
                Arguments.of(
                    Quantity(4f, Volume.USCups),
                    Quantity(3.3307f, Volume.ImperialCups)
                ),
                Arguments.of(
                    Quantity(4f, Volume.ImperialOunces),
                    Quantity(0.2f, Volume.ImperialPints)
                ),
                Arguments.of(
                    Quantity(1f, Volume.ImperialQuarts),
                    Quantity(1136.52f, Volume.Milliliter)
                ),
                Arguments.of(
                    Quantity(1f, Volume.USTablespoons),
                    Quantity(3f, Volume.USTeaspoons)
                ),
                Arguments.of(
                    Quantity(1f, Volume.ImperialTablespoons),
                    Quantity(3f, Volume.ImperialTeaspoons)
                )
            )
        }

    }
}