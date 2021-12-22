package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.toUTC
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.PressureUnits
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream

internal class OceanographyServiceTest {
    @Test
    fun getTidalRange() {
        val service = OceanographyService()

        val cases = listOf(
            Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 13, 6, 0), TidalRange.Neap),
            Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 17, 6, 0), TidalRange.Spring),
            Pair(LocalDateTime.of(2020, Month.SEPTEMBER, 8, 6, 0), TidalRange.Normal)
        )

        for (case in cases) {
            val tide =
                service.getTidalRange(ZonedDateTime.of(case.first, ZoneId.of("America/New_York")))
            Assert.assertEquals(case.second, tide)
        }

    }

    @ParameterizedTest
    @MethodSource("provideTides")
    fun getTides(reference: ZonedDateTime, date: LocalDate, expected: List<Tide>) {
        val service = OceanographyService()

        val tides = service.getTides(reference, TideFrequency.Semidiurnal, date)

        Assert.assertEquals(expected.size, tides.size)

        for (i in tides.indices) {
            Assert.assertEquals(expected[i].type, tides[i].type)
            timeEquals(tides[i].time, expected[i].time, Duration.ofHours(2))
        }

    }

    @ParameterizedTest
    @MethodSource("provideNextTide")
    fun getNextTide(reference: ZonedDateTime, time: ZonedDateTime, expected: Tide) {
        val service = OceanographyService()

        val tide = service.getNextTide(reference, TideFrequency.Semidiurnal, time)

        Assert.assertEquals(expected.type, tide?.type)
        timeEquals(tide?.time, expected.time, Duration.ofHours(2))
    }

    @ParameterizedTest
    @MethodSource("provideTideType")
    fun getTideType(reference: ZonedDateTime, time: ZonedDateTime, expected: TideType) {
        val service = OceanographyService()

        val tide = service.getTideType(reference, TideFrequency.Semidiurnal, time)

        Assert.assertEquals(expected, tide)
    }

    @Test
    fun getWaterLevel() {
        val m = 0f// 1.86f - 0.23f
        val reference =
            LocalDateTime.of(2021, 12, 22, 2, 15).toZonedDateTime().plusHours(12).plusMinutes(25)//.toUniversalTime().toUTC()
//        val now = LocalDateTime.of(2021, 12, 22, 2, 53).toZonedDateTime()
        val harmonics = listOf(
            TidalHarmonic(1.5f, 8.2f, 28.984104f),
            TidalHarmonic(0.32f, 28.2f, 30f),
            TidalHarmonic(0.36f, 349.2f, 28.43973f),
            TidalHarmonic(0.21f, 169.9f, 15.041069f),
            TidalHarmonic(0.08f, 23.9f, 57.96821f),
            TidalHarmonic(0.16f, 198.1f, 13.943035f),
            TidalHarmonic(0.04f, 196.9f, 86.95232f),
            TidalHarmonic(0.01f, 27.6f, 60f),
            TidalHarmonic(0.02f, 115.4f, 58.984104f),
            TidalHarmonic(m, 0f, 0f)
        )

        val now = LocalDateTime.of(2021, 12, 22, 15, 54).toZonedDateTime()
            .withZoneSameInstant(ZoneId.of("UTC"))
        val level = OceanographyService().getWaterLevel(now, reference, harmonics)
        println(level)

    }


    private fun timeEquals(actual: ZonedDateTime?, expected: ZonedDateTime?, precision: Duration) {
        val duration = Duration.between(actual, expected).abs()
        Assert.assertEquals(
            "Expected $actual to equal $expected",
            0.0,
            duration.toMillis().toDouble(),
            precision.toMillis().toDouble()
        )
    }

    @Test
    fun canCalculateDepth() {
        val currentPressure = Pressure(2222.516f, PressureUnits.Hpa)
        val service = OceanographyService()

        val depth = service.getDepth(currentPressure, Pressure(1013f, PressureUnits.Hpa))

        val expected = Distance(12f, DistanceUnits.Meters)

        Assert.assertEquals(expected.distance, depth.distance, 0.1f)
    }

    @Test
    fun depthReturnsZeroWhenAboveWater() {
        val currentPressure = Pressure(1000f, PressureUnits.Hpa)
        val service = OceanographyService()

        val depth = service.getDepth(currentPressure, Pressure(1013f, PressureUnits.Hpa))

        val expected = Distance(0f, DistanceUnits.Meters)

        Assert.assertEquals(expected, depth)
    }


    companion object {

        @JvmStatic
        fun provideTides(): Stream<Arguments> {
            val reference1 = time(2021, 3, 15, 10, 7, "America/New_York")
            return Stream.of(
                Arguments.of(
                    reference1, LocalDate.of(2021, 3, 16), listOf(
                        Tide.low(time(2021, 3, 16, 4, 31, "America/New_York")),
                        Tide.high(time(2021, 3, 16, 10, 43, "America/New_York")),
                        Tide.low(time(2021, 3, 16, 16, 9, "America/New_York")),
                        Tide.high(time(2021, 3, 16, 22, 57, "America/New_York"))
                    )
                )
            )
        }

        @JvmStatic
        fun provideNextTide(): Stream<Arguments> {
            val reference1 = time(2021, 3, 15, 10, 7, "America/New_York")
            return Stream.of(
                Arguments.of(
                    reference1, time(2021, 3, 16, 2, 0, "America/New_York"),
                    Tide.low(time(2021, 3, 16, 4, 31, "America/New_York"))
                ),
                Arguments.of(
                    reference1, time(2021, 3, 16, 23, 50, "America/New_York"),
                    Tide.low(time(2021, 3, 17, 5, 4, "America/New_York"))
                ),
                Arguments.of(
                    reference1, time(2021, 3, 14, 21, 0, "America/New_York"),
                    Tide.high(time(2021, 3, 14, 21, 50, "America/New_York"))
                )
            )
        }

        @JvmStatic
        fun provideTideType(): Stream<Arguments> {
            val reference1 = time(2021, 3, 15, 10, 7, "America/New_York")
            return Stream.of(
                Arguments.of(
                    reference1, time(2021, 3, 16, 2, 0, "America/New_York"),
                    TideType.Half
                ),
                Arguments.of(
                    reference1, time(2021, 3, 16, 3, 0, "America/New_York"),
                    TideType.Low
                ),
                Arguments.of(
                    reference1, time(2021, 3, 14, 21, 0, "America/New_York"),
                    TideType.High
                )
            )
        }

        private fun time(
            year: Int,
            month: Int,
            day: Int,
            hour: Int,
            minute: Int,
            zone: String
        ): ZonedDateTime {
            return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.of(zone))
        }

    }

}