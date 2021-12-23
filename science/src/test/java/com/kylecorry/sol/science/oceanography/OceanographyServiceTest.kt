package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.science.astronomy.AstronomyService
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.toUTC
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.units.*
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream
import kotlin.math.absoluteValue

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
        val harmonics = listOf(
            TidalHarmonic(TideConstituent.M2, 1.66f, 2.3f),
            TidalHarmonic(TideConstituent.S2, 0.35f, 25f),
            TidalHarmonic(TideConstituent.N2, 0.41f, 345.8f),
            TidalHarmonic(TideConstituent.K1, 0.2f, 166.1f),
            TidalHarmonic(TideConstituent.O1, 0.15f, 202f),
            TidalHarmonic(TideConstituent.P1, 0.07f, 176.6f),
            TidalHarmonic(TideConstituent.M4, 0.19f, 35.8f),
            TidalHarmonic(TideConstituent.K2, 0.1f, 21.7f),
            TidalHarmonic(TideConstituent.L2, 0.04f, 349.9f),
            TidalHarmonic(TideConstituent.MS4, 0.05f, 106.4f),
            TidalHarmonic(TideConstituent.Z0, 0f, 0f)
        )

        val zone = ZoneId.of("America/New_York")
        val date = LocalDate.of(2021, 12, 22)


        val service = OceanographyService()
        val delta = 0.35f
        assertEquals(service.getWaterLevel(ZonedDateTime.of(date, LocalTime.of(2, 35), zone), harmonics), -1.69f, delta)
        assertEquals(service.getWaterLevel(ZonedDateTime.of(date, LocalTime.of( 9, 27), zone), harmonics), 1.59f, delta)
        assertEquals(service.getWaterLevel(ZonedDateTime.of(date, LocalTime.of( 15, 27), zone), harmonics), -1.59f, delta)
        assertEquals(service.getWaterLevel(ZonedDateTime.of(date, LocalTime.of( 22, 0), zone), harmonics), 1.13f, delta)
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