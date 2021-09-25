package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.science.meteorology.forecast.Weather
import com.kylecorry.sol.units.*
import com.kylecorry.sol.science.shared.Season
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream

class WeatherServiceTest {

    private val weatherService = WeatherService()

    @ParameterizedTest
    @MethodSource("provideForecasts")
    fun forecast(
        characteristic: PressureCharacteristic,
        tendencyAmount: Float,
        stormThresh: Float?,
        expectedWeather: Weather
    ) {
        val tendency = PressureTendency(characteristic, tendencyAmount)
        val weather = weatherService.forecast(tendency, stormThresh)
        assertEquals(expectedWeather, weather)
    }

    @ParameterizedTest
    @MethodSource("provideTendencies")
    fun tendency(
        last: Pressure,
        current: Pressure,
        duration: Duration,
        threshold: Float,
        expectedTendency: PressureTendency
    ) {
        val tendency = weatherService.getTendency(last, current, duration, threshold)
        assertEquals(expectedTendency, tendency)
    }

    @ParameterizedTest
    @MethodSource("provideHeatIndex")
    fun heatIndex(temperature: Float, humidity: Float, expected: Float) {
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
    fun dewPoint(temperature: Float, humidity: Float, expected: Float) {
        val dew = weatherService.getDewPoint(temperature, humidity)
        assertEquals(expected, dew, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideLightningStrikes")
    fun lightningStrikes(lightning: Instant, thunder: Instant, expected: Float) {
        val distance = weatherService.getLightningStrikeDistance(lightning, thunder)
        assertEquals(expected, distance, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideLightningStrikeDistances")
    fun lightningStrikeDanger(distance: Distance, expected: Boolean) {
        val danger = weatherService.isLightningStrikeDangerous(distance)
        assertEquals(expected, danger)
    }

    @ParameterizedTest
    @MethodSource("provideSeasons")
    fun seasons(expected: Season, isNorth: Boolean, date: LocalDate) {
        val season = weatherService.getSeason(
            Coordinate(if (isNorth) 1.0 else -1.0, 0.0),
            ZonedDateTime.of(date, LocalTime.MIN, ZoneId.systemDefault())
        )
        Assert.assertEquals(expected, season)
    }

    @Test
    fun ambientTemperature() {
        val temp = weatherService.getAmbientTemperature(170f, 125f, 100f)
        assertEquals(68.75f, temp)
    }

    @ParameterizedTest
    @MethodSource("provideSeaLevelPressure")
    fun convertsToSeaLevel(
        pressure: Pressure,
        altitude: Distance,
        temperature: Temperature?,
        expected: Pressure
    ) {
        val reading = weatherService.getSeaLevelPressure(pressure, altitude, temperature)
        assertEquals(expected.pressure, reading.pressure, 0.1f)
        assertEquals(expected.units, reading.units)
    }

    @ParameterizedTest
    @MethodSource("provideHighPressures")
    fun isHigh(pressure: Pressure, isHigh: Boolean) {
        val ret = weatherService.isHighPressure(pressure)
        assertEquals(isHigh, ret)
    }

    @ParameterizedTest
    @MethodSource("provideLowPressures")
    fun isLow(pressure: Pressure, isLow: Boolean) {
        val ret = weatherService.isLowPressure(pressure)
        assertEquals(isLow, ret)
    }


    companion object {

        @JvmStatic
        fun provideLowPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Pressure(1000f, PressureUnits.Hpa), true),
                Arguments.of(Pressure(1009.144f, PressureUnits.Hpa), true),
                Arguments.of(Pressure(1009.145f, PressureUnits.Hpa), false),
                Arguments.of(Pressure(1013f, PressureUnits.Hpa), false),
                Arguments.of(Pressure(1030f, PressureUnits.Hpa), false),
            )
        }

        @JvmStatic
        fun provideHighPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Pressure(1000f, PressureUnits.Hpa), false),
                Arguments.of(Pressure(1013f, PressureUnits.Hpa), false),
                Arguments.of(Pressure(1022.688f, PressureUnits.Hpa), false),
                Arguments.of(Pressure(1022.689f, PressureUnits.Hpa), true),
                Arguments.of(Pressure(1030f, PressureUnits.Hpa), true),
            )
        }

        @JvmStatic
        fun provideSeaLevelPressure(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Pressure(0f, PressureUnits.Hpa),
                    Distance.meters(0f),
                    null,
                    Pressure(0f, PressureUnits.Hpa)
                ),
                Arguments.of(
                    Pressure(0f, PressureUnits.Hpa),
                    Distance.meters(0f),
                    Temperature(0f, TemperatureUnits.C),
                    Pressure(0f, PressureUnits.Hpa)
                ),
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Distance.meters(-100f),
                    null,
                    Pressure(988.2f, PressureUnits.Hpa)
                ),
                Arguments.of(
                    Pressure(980f, PressureUnits.Hpa),
                    Distance.meters(200f),
                    null,
                    Pressure(1003.48f, PressureUnits.Hpa)
                ),
                Arguments.of(
                    Pressure(980f, PressureUnits.Hpa),
                    Distance.meters(1000f),
                    Temperature(15f, TemperatureUnits.C),
                    Pressure(1101.93f, PressureUnits.Hpa)
                ),
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Distance.meters(-100f),
                    Temperature(28f, TemperatureUnits.C),
                    Pressure(988.71f, PressureUnits.Hpa)
                ),
            )
        }

        @JvmStatic
        fun provideForecasts(): Stream<Arguments> {
            // First argument not used yet
            return Stream.of(
                Arguments.of(PressureCharacteristic.Steady, 0f, -6f, Weather.NoChange),
                Arguments.of(PressureCharacteristic.FallingFast, -6f, -6f, Weather.Storm),
                Arguments.of(PressureCharacteristic.FallingFast, -8f, -7f, Weather.Storm),
                Arguments.of(PressureCharacteristic.FallingFast, -6f, null, Weather.Storm),
                Arguments.of(
                    PressureCharacteristic.FallingFast,
                    -5f,
                    null,
                    Weather.WorseningFast
                ),
                Arguments.of(
                    PressureCharacteristic.FallingFast,
                    -4f,
                    -6f,
                    Weather.WorseningFast
                ),
                Arguments.of(PressureCharacteristic.Falling, -2f, -6f, Weather.WorseningSlow),
                Arguments.of(PressureCharacteristic.Rising, 2f, -6f, Weather.ImprovingSlow),
                Arguments.of(
                    PressureCharacteristic.RisingFast,
                    8f,
                    -6f,
                    Weather.ImprovingFast
                ),
            )
        }

        @JvmStatic
        fun provideTendencies(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Pressure(1001f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 1f)
                ),
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Pressure(1004f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.RisingFast, 4f)
                ),
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Pressure(1003f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.Rising, 3f)
                ),
                Arguments.of(
                    Pressure(1004f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.FallingFast, -4f)
                ),
                Arguments.of(
                    Pressure(1002f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.Falling, -2f)
                ),
                Arguments.of(
                    Pressure(1002f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    1f,
                    PressureTendency(PressureCharacteristic.Falling, -2f)
                ),
                Arguments.of(
                    Pressure(1003f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(3),
                    1f,
                    PressureTendency(PressureCharacteristic.FallingFast, -3f)
                ),
                Arguments.of(
                    Pressure(1002f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(2),
                    1f,
                    PressureTendency(PressureCharacteristic.FallingFast, -3f)
                ),
                Arguments.of(
                    Pressure(1008f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ofHours(4),
                    2f,
                    PressureTendency(PressureCharacteristic.FallingFast, -6f)
                ),
                Arguments.of(
                    Pressure(1000f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ZERO,
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    Pressure(1000.1f, PressureUnits.Hpa),
                    Pressure(1000f, PressureUnits.Hpa),
                    Duration.ZERO,
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
        fun provideHeatAlert(): Stream<Arguments> {
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

        @JvmStatic
        fun provideLightningStrikeDistances(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Distance.kilometers(10f), true),
                Arguments.of(Distance.kilometers(10.1f), false),
                Arguments.of(Distance.meters(10000f), true),
                Arguments.of(Distance.meters(100f), true),
            )
        }

        @JvmStatic
        fun provideSeasons(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Season.Summer, false, LocalDate.of(2021, 1, 1)),
                Arguments.of(Season.Summer, false, LocalDate.of(2021, 2, 28)),
                Arguments.of(Season.Fall, false, LocalDate.of(2021, 3, 1)),
                Arguments.of(Season.Fall, false, LocalDate.of(2021, 5, 31)),
                Arguments.of(Season.Winter, false, LocalDate.of(2021, 6, 1)),
                Arguments.of(Season.Winter, false, LocalDate.of(2021, 8, 31)),
                Arguments.of(Season.Spring, false, LocalDate.of(2021, 9, 1)),
                Arguments.of(Season.Spring, false, LocalDate.of(2021, 11, 30)),
                Arguments.of(Season.Summer, false, LocalDate.of(2021, 12, 1)),
                Arguments.of(Season.Summer, false, LocalDate.of(2021, 12, 31)),

                Arguments.of(Season.Winter, true, LocalDate.of(2021, 1, 1)),
                Arguments.of(Season.Winter, true, LocalDate.of(2021, 2, 28)),
                Arguments.of(Season.Spring, true, LocalDate.of(2021, 3, 1)),
                Arguments.of(Season.Spring, true, LocalDate.of(2021, 5, 31)),
                Arguments.of(Season.Summer, true, LocalDate.of(2021, 6, 1)),
                Arguments.of(Season.Summer, true, LocalDate.of(2021, 8, 31)),
                Arguments.of(Season.Fall, true, LocalDate.of(2021, 9, 1)),
                Arguments.of(Season.Fall, true, LocalDate.of(2021, 11, 30)),
                Arguments.of(Season.Winter, true, LocalDate.of(2021, 12, 1)),
                Arguments.of(Season.Winter, true, LocalDate.of(2021, 12, 31)),
            )
        }
    }

}