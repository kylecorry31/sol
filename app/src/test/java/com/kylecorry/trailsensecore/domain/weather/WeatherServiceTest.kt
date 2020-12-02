package com.kylecorry.trailsensecore.domain.weather

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import java.util.stream.Stream

class WeatherServiceTest {

    private val weatherService = WeatherService()

    @ParameterizedTest
    @MethodSource("provideForecasts")
    fun forecast(
        pressure: Float,
        characteristic: PressureCharacteristic,
        tendencyAmount: Float,
        stormThresh: Float?,
        expectedWeather: Weather
    ) {
        val tendency = PressureTendency(characteristic, tendencyAmount)
        val reading = PressureReading(Instant.now(), pressure)
        val weather = weatherService.forecast(tendency, reading, stormThresh)
        assertEquals(expectedWeather, weather)
    }

    @ParameterizedTest
    @MethodSource("provideTendencies")
    fun tendency(
        last: PressureReading,
        current: PressureReading,
        threshold: Float,
        expectedTendency: PressureTendency
    ) {
        val tendency = weatherService.getTendency(last, current, threshold)
        assertEquals(expectedTendency, tendency)
    }

    @ParameterizedTest
    @MethodSource("provideHeatIndex")
    fun heatIndex(temperature: Float, humidity: Float, expected: Float){
        val hi = weatherService.getHeatIndex(temperature, humidity)
        assertEquals(expected, hi, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideHeatAlert")
    fun heatAlert(heatIndex: Float, expected: HeatAlert) {
        val alert = weatherService.getHeatAlert(heatIndex)
        assertEquals(expected, alert)
    }

    @ParameterizedTest
    @MethodSource("provideDewPoint")
    fun dewPoint(temperature: Float, humidity: Float, expected: Float){
        val dew = weatherService.getDewPoint(temperature, humidity)
        assertEquals(expected, dew, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideLightningStrikes")
    fun lightningStrikes(lightning: Instant, thunder: Instant, expected: Float){
        val distance = weatherService.getLightningStrikeDistance(lightning, thunder)
        assertEquals(expected, distance, 0.5f)
    }


    companion object {
        @JvmStatic
        fun provideForecasts(): Stream<Arguments> {
            // First argument not used yet
            return Stream.of(
                Arguments.of(1013, PressureCharacteristic.Steady, 0f, -6f, Weather.NoChange),
                Arguments.of(1013, PressureCharacteristic.FallingFast, -6f, -6f, Weather.Storm),
                Arguments.of(1013, PressureCharacteristic.FallingFast, -8f, -7f, Weather.Storm),
                Arguments.of(1013, PressureCharacteristic.FallingFast, -6f, null, Weather.Storm),
                Arguments.of(
                    1013,
                    PressureCharacteristic.FallingFast,
                    -5f,
                    null,
                    Weather.WorseningFast
                ),
                Arguments.of(
                    1013,
                    PressureCharacteristic.FallingFast,
                    -4f,
                    -6f,
                    Weather.WorseningFast
                ),
                Arguments.of(1013, PressureCharacteristic.Falling, -2f, -6f, Weather.WorseningSlow),
                Arguments.of(1013, PressureCharacteristic.Rising, 2f, -6f, Weather.ImprovingSlow),
                Arguments.of(
                    1013,
                    PressureCharacteristic.RisingFast,
                    8f,
                    -6f,
                    Weather.ImprovingFast
                ),
            )
        }

        @JvmStatic
        fun provideTendencies(): Stream<Arguments> {
            val now = Instant.now()
            val h3 = now.minusSeconds(60 * 60 * 3)
            val h2 = now.minusSeconds(60 * 60 * 2)
            val h4 = now.minusSeconds(60 * 60 * 4)

            return Stream.of(
                Arguments.of(
                    PressureReading(h3, 1000f),
                    PressureReading(now, 1000f),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    PressureReading(h3, 1000f),
                    PressureReading(now, 1001f),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 1f)
                ),
                Arguments.of(
                    PressureReading(h3, 1000f),
                    PressureReading(now, 1004f),
                    2f,
                    PressureTendency(PressureCharacteristic.RisingFast, 4f)
                ),
                Arguments.of(
                    PressureReading(h3, 1000f),
                    PressureReading(now, 1003f),
                    2f,
                    PressureTendency(PressureCharacteristic.Rising, 3f)
                ),
                Arguments.of(
                    PressureReading(h3, 1004f),
                    PressureReading(now, 1000f),
                    2f,
                    PressureTendency(PressureCharacteristic.FallingFast, -4f)
                ),
                Arguments.of(
                    PressureReading(h3, 1002f),
                    PressureReading(now, 1000f),
                    2f,
                    PressureTendency(PressureCharacteristic.Falling, -2f)
                ),
                Arguments.of(
                    PressureReading(h3, 1002f),
                    PressureReading(now, 1000f),
                    1f,
                    PressureTendency(PressureCharacteristic.Falling, -2f)
                ),
                Arguments.of(
                    PressureReading(h3, 1003f),
                    PressureReading(now, 1000f),
                    1f,
                    PressureTendency(PressureCharacteristic.FallingFast, -3f)
                ),
                Arguments.of(
                    PressureReading(h2, 1002f),
                    PressureReading(now, 1000f),
                    1f,
                    PressureTendency(PressureCharacteristic.FallingFast, -3f)
                ),
                Arguments.of(
                    PressureReading(h4, 1008f),
                    PressureReading(now, 1000f),
                    2f,
                    PressureTendency(PressureCharacteristic.FallingFast, -6f)
                ),
                Arguments.of(
                    PressureReading(now, 1000f),
                    PressureReading(now, 1000f),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    PressureReading(now, 1000.1f),
                    PressureReading(now, 1000f),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
            )
        }

        @JvmStatic
        fun provideHeatIndex(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, 0f, 0f),
                Arguments.of(1f, 0f, 1f),
                Arguments.of(26f, 0f, 26f),
                Arguments.of(27f, 80f, 30f),
                Arguments.of(30f, 80f, 38f),
                Arguments.of(30f, 100f, 44f),
                Arguments.of(30f, 45f, 30f),
                Arguments.of(36f, 60f, 48f),
            )
        }

        @JvmStatic
        fun provideHeatAlert(): Stream<Arguments>{
            return Stream.of(
                Arguments.of(-26f, HeatAlert.FrostbiteDanger),
                Arguments.of(-25f, HeatAlert.FrostbiteDanger),
                Arguments.of(-24f, HeatAlert.FrostbiteWarning),
                Arguments.of(-17f, HeatAlert.FrostbiteWarning),
                Arguments.of(-16f, HeatAlert.FrostbiteCaution),
                Arguments.of(5f, HeatAlert.FrostbiteCaution),
                Arguments.of(6f, HeatAlert.Normal),
                Arguments.of(26f, HeatAlert.Normal),
                Arguments.of(27f, HeatAlert.HeatCaution),
                Arguments.of(32.5f, HeatAlert.HeatCaution),
                Arguments.of(33f, HeatAlert.HeatWarning),
                Arguments.of(39f, HeatAlert.HeatWarning),
                Arguments.of(40f, HeatAlert.HeatAlert),
                Arguments.of(50f, HeatAlert.HeatAlert),
                Arguments.of(51f, HeatAlert.HeatDanger),
            )
        }

        @JvmStatic
        fun provideDewPoint(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(20f, 100f, 20f),
                Arguments.of(21f, 90f, 19f),
                Arguments.of(15f, 24f, -5f),
                Arguments.of(16f, 10f, -16f),
            )
        }

        @JvmStatic
        fun provideLightningStrikes(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Instant.ofEpochSecond(0), Instant.ofEpochSecond(1), 343f),
                Arguments.of(Instant.ofEpochSecond(0), Instant.ofEpochSecond(2), 686f),
                Arguments.of(Instant.ofEpochSecond(0), Instant.ofEpochSecond(10), 3430f),
                Arguments.of(Instant.ofEpochSecond(0), Instant.ofEpochSecond(0), 0f),
                Arguments.of(Instant.ofEpochSecond(1), Instant.ofEpochSecond(0), 0f),
            )
        }
    }

}