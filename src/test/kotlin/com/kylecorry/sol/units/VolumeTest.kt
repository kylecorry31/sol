package com.kylecorry.sol.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class VolumeTest {
    @ParameterizedTest
    @MethodSource("provideConversions")
    fun convertTo(volume: Float, units: VolumeUnits, expectedVolume: Float, expectedUnits: VolumeUnits) {
        val converted = Volume.from(volume, units).convertTo(expectedUnits)
        assertEquals(expectedUnits, converted.units)
        assertEquals(expectedVolume, converted.value, 0.0001f)
    }

    companion object {

        @JvmStatic
        fun provideConversions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    2f, VolumeUnits.Liters,
                    0.528344f, VolumeUnits.USGallons
                ),
                Arguments.of(
                    3f, VolumeUnits.USGallons,
                    11.35623f, VolumeUnits.Liters
                ),
                Arguments.of(
                    3f, VolumeUnits.ImperialGallons,
                    461.16525f, VolumeUnits.USOunces
                ),
                Arguments.of(
                    4f, VolumeUnits.Milliliter,
                    0.014078f, VolumeUnits.ImperialCups
                ),
                Arguments.of(4f, VolumeUnits.USPints, 2f, VolumeUnits.USQuarts),
                Arguments.of(
                    4f, VolumeUnits.USCups,
                    3.3307f, VolumeUnits.ImperialCups
                ),
                Arguments.of(
                    4f, VolumeUnits.ImperialOunces,
                    0.2f, VolumeUnits.ImperialPints
                ),
                Arguments.of(
                    1f, VolumeUnits.ImperialQuarts,
                    1136.52f, VolumeUnits.Milliliter
                ),
                Arguments.of(
                    1f, VolumeUnits.USTablespoons,
                    3f, VolumeUnits.USTeaspoons
                ),
                Arguments.of(
                    1f, VolumeUnits.ImperialTablespoons,
                    3f, VolumeUnits.ImperialTeaspoons
                )
            )
        }

    }
}