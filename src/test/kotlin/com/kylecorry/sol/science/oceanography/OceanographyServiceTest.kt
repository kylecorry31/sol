package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.math.statistics.Statistics
import com.kylecorry.sol.science.oceanography.waterlevel.*
import com.kylecorry.sol.time.Time.atEndOfDay
import com.kylecorry.sol.time.Time.roundNearestMinute
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import java.io.File
import java.net.http.HttpClient
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
            assertEquals(case.second, tide)
        }

    }

    @Test
    fun getTidesHarmonics() {
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
        val date = LocalDate.of(2023, 12, 22).atStartOfDay(zone)

        val expected = listOf(
            Tide.high(ZonedDateTime.of(date.toLocalDate(), LocalTime.of(3, 31), zone)),
            Tide.low(ZonedDateTime.of(date.toLocalDate(), LocalTime.of(10, 23), zone)),
            Tide.high(ZonedDateTime.of(date.toLocalDate(), LocalTime.of(15, 56), zone)),
            Tide.low(ZonedDateTime.of(date.toLocalDate(), LocalTime.of(21, 36), zone))
        )

        val service = OceanographyService()

        val tides = service.getTides(
            HarmonicWaterLevelCalculator(harmonics),
            date,
            date.atEndOfDay()
        )

        assertEquals(expected.size, tides.size)

        for (i in tides.indices) {
            assertEquals(expected[i].isHigh, tides[i].isHigh)
            timeEquals(tides[i].time, expected[i].time, Duration.ofMinutes(30))
        }
    }

    @Test
    fun canGetTides() {
        val service = OceanographyService()
        val location = Coordinate(42.8150, -70.8733)
        val start = ZonedDateTime.of(2024, 7, 1, 0, 0, 0, 0, ZoneId.of("America/New_York"))
        val end = ZonedDateTime.of(2024, 7, 29, 0, 0, 0, 0, ZoneId.of("America/New_York"))
        val station = "8452660"
        val url =
            "https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?product=predictions&application=NOS.COOPS.TAC.WL&begin_date=20240701&end_date=20240730&datum=MLLW&station=$station&time_zone=lst_ldt&units=english&interval=hilo&format=csv"
        File("temp").mkdirs()

        val sourceString = if (File("temp/${station}.csv").exists()) {
            File("temp/${station}.csv").readText()
        } else {
            // Download the file
            val httpClient = HttpClient.newHttpClient()
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .build()
            val response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            val body = response.body()
            File("temp/${station}.csv").writeText(body)
            body
        }

        val source = sourceString.trim().lines().drop(1).map {
            val parts = it.split(",")
            Tide(
                LocalDateTime.parse(parts[0].replace(" ", "T")).atZone(ZoneId.of("America/New_York")),
                parts[2] == "H",
                parts[1].toFloat()
            )
        }

        val highs = source.filter { it.isHigh }.map { it.time }
        val lows = source.filter { !it.isHigh }.map { it.time }
        val localLunitidalInterval = service.getMeanLunitidalInterval(highs, location) ?: Duration.ZERO
        val localLowLunitidalInterval = service.getMeanLunitidalInterval(lows, location) ?: Duration.ZERO
        val utcLunitidalInterval = service.getMeanLunitidalInterval(highs) ?: Duration.ZERO
        val utcLowLunitidalInterval = service.getMeanLunitidalInterval(lows) ?: Duration.ZERO

        println("Local lunitidal interval: $localLunitidalInterval")
        println("UTC lunitidal interval: $utcLunitidalInterval")

        val calculators = listOf(
            "LUNITIDAL HARMONIC (LOCAL)" to HarmonicLunitidalWaterLevelCalculator(localLunitidalInterval, location),
            "LUNITIDAL HARMONIC (UTC)" to HarmonicLunitidalWaterLevelCalculator(utcLunitidalInterval),
            "TIDE CLOCK" to TideClockWaterLevelCalculator(Tide.high(highs.first()), TideConstituent.M2.speed),
            "LUNITIDAL (LOCAL)" to LunitidalWaterLevelCalculator(localLunitidalInterval, location),
            "LUNITIDAL (LOCAL; HIGH AND LOW)" to LunitidalWaterLevelCalculator(
                localLunitidalInterval,
                location,
                localLowLunitidalInterval
            ),
            "LUNITIDAL (UTC)" to LunitidalWaterLevelCalculator(
                utcLunitidalInterval,
                lowLunitidalInterval = utcLowLunitidalInterval
            ),
        )

        calculators.forEach { (name, calculator) ->
            testCalculator(name, calculator, start, end, source)
        }
    }

    private fun testCalculator(
        name: String,
        calculator: IWaterLevelCalculator,
        start: ZonedDateTime,
        end: ZonedDateTime,
        references: List<Tide>
    ) {
        val service = OceanographyService()
        val tides = service.getTides(
            calculator,
            start,
            end
        )

        val deltas = mutableListOf<Float>()


        for (i in tides.indices) {
            val tide = tides[i].time.roundNearestMinute()
            val refTide =
                references.filter { it.isHigh == tides[i].isHigh }
                    .minByOrNull { Duration.between(it.time, tide).abs() }!!
            val d = Duration.between(refTide.time, tide).abs()
            deltas.add(d.toMinutes().toFloat())
        }

        val average = Statistics.mean(deltas)
        val max = deltas.max()
        val quantile95 = Statistics.quantile(deltas, 0.95f)
        val quantile80 = Statistics.quantile(deltas, 0.8f)
        val quantile50 = Statistics.quantile(deltas, 0.5f)
        println(name)
        println("Average: $average")
        println("Max: $max")
        println("95th percentile: $quantile95")
        println("80th percentile: $quantile80")
        println("50th percentile: $quantile50")
        println()

        assertTrue(average < 35)
        assertTrue(max < 90)
        assertTrue(quantile95 < 70)
        assertTrue(quantile80 < 60)
        assertTrue(quantile50 < 30)
    }


    //    @Test
    fun generateHarmonicFile() {
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
        val date = LocalDate.of(2021, 1, 1)


        val service = OceanographyService()

        val levels = mutableListOf<Float>()

        val file = File("harmonics.csv")
        var time = date.atStartOfDay()
        while (time.toLocalDate() <= date.plusDays(30)) {
            val level = HarmonicWaterLevelCalculator(harmonics).calculate(
                ZonedDateTime.of(time, zone)
            )

            levels.add(level)

            time = time.plusMinutes(1)
        }

        file.writeText(levels.joinToString(","))
    }

    @Test
    fun getWaterLevel() {

    }


    private fun timeEquals(actual: ZonedDateTime?, expected: ZonedDateTime?, precision: Duration) {
        val duration = Duration.between(actual, expected).abs()
        assertEquals(
            0.0,
            duration.toMillis().toDouble(),
            precision.toMillis().toDouble(),
            "Expected $actual to equal $expected"
        )
    }

    @Test
    fun canCalculateDepth() {
        val currentPressure = Pressure(2222.516f, PressureUnits.Hpa)
        val service = OceanographyService()

        val depth = service.getDepth(currentPressure, Pressure(1013f, PressureUnits.Hpa))

        val expected = Distance(12f, DistanceUnits.Meters)

        assertEquals(expected.distance, depth.distance, 0.1f)
    }

    @Test
    fun depthReturnsZeroWhenAboveWater() {
        val currentPressure = Pressure(1000f, PressureUnits.Hpa)
        val service = OceanographyService()

        val depth = service.getDepth(currentPressure, Pressure(1013f, PressureUnits.Hpa))

        val expected = Distance(0f, DistanceUnits.Meters)

        assertEquals(expected, depth)
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