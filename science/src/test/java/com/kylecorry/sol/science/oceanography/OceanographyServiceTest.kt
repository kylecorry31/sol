package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.science.astronomy.AstronomyService
import com.kylecorry.sol.science.astronomy.units.toUniversalTime
import com.kylecorry.sol.time.Time.atStartOfDay
import com.kylecorry.sol.time.Time.toUTC
import com.kylecorry.sol.time.Time.toZonedDateTime
import com.kylecorry.sol.units.*
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
            TidalHarmonic(1.66f, 2.3f, 28.984104f),
            TidalHarmonic(0.35f, 25f, 30f),
            TidalHarmonic(0.41f, 345.8f, 28.43973f),
            TidalHarmonic(0.2f, 166.1f, 15.041069f),
            TidalHarmonic(0.19f, 35.8f, 57.96821f),
            TidalHarmonic(0.15f, 202f, 13.943035f),
            TidalHarmonic(0.02f, 220.1f, 86.95232f)
        )

        val now = LocalDateTime.of(2021, 12, 22, 12, 35).toZonedDateTime()

        /**
         *
        2:35 AM	low	0.04 ft.
        9:27 AM	high	3.33 ft.
        3:27 PM	low	0.14 ft.
        10:00 PM	high	2.87 ft.
         */


        val astro = AstronomyService()
        val loc = Coordinate.zero
        val lunarNoon = astro.getMoonEvents(now, loc).transit!!

        val ref = LocalDateTime.of(1983, 1, 1, 0, 0).toZonedDateTime()


        var last = OceanographyService().getWaterLevel(now.atStartOfDay(), ref, harmonics)
        val last2 = OceanographyService().getWaterLevel(now.atStartOfDay().plusSeconds(30), ref, harmonics)

        var decreasing = last2 < last

        for (i in 0..(24 * 60)){
            val level = OceanographyService().getWaterLevel(now.atStartOfDay().plusMinutes(i.toLong()), ref, harmonics)

            if (decreasing && level > last){
                println("High: ${now.atStartOfDay().plusMinutes(i.toLong())}")
                decreasing = false
            } else if (!decreasing && level < last){
                println("Low: ${now.atStartOfDay().plusMinutes(i.toLong())}")
                decreasing = true
            }

            last = level

//            println("$i: ${level.roundPlaces(1)}")
        }

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