package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.units.*
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.time.Time
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*
import java.util.stream.Stream

class MeteorologyTest {

    @ParameterizedTest
    @MethodSource("provideForecasts")
    fun forecast(
        characteristic: PressureCharacteristic,
        tendencyAmount: Float,
        stormThresh: Float?,
        expectedWeather: Weather
    ) {
        val tendency = PressureTendency(characteristic, tendencyAmount)
        val weather = Meteorology.forecast(tendency, stormThresh)
        assertEquals(expectedWeather, weather)
    }

    @ParameterizedTest
    @MethodSource("provideWeatherForecasts")
    fun forecast(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        temperatures: Range<Temperature>?,
        now: WeatherForecast,
        then: WeatherForecast
    ) {
        val weather = Meteorology.forecast(pressures, clouds, temperatures, time = weatherTime)
        assertEquals(now, weather.first())
        assertEquals(then, weather.last())
    }

    @ParameterizedTest
    @MethodSource("provideTendencies")
    fun tendency(
        lastHpa: Float,
        currentHpa: Float,
        duration: Duration,
        threshold: Float,
        expectedTendency: PressureTendency
    ) {
        val tendency = Meteorology.getTendency(Pressure.hpa(lastHpa), Pressure.hpa(currentHpa), duration, threshold)
        assertEquals(expectedTendency, tendency)
    }

    @ParameterizedTest
    @MethodSource("provideHeatIndex")
    fun heatIndex(temperature: Float, humidity: Float, expected: Float) {
        val hi = Meteorology.getHeatIndex(temperature, humidity)
        assertEquals(expected, hi, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideHeatAlert")
    fun heatAlert(heatIndex: Float, expected: HeatAlert) {
        val alert = Meteorology.getHeatAlert(heatIndex)
        assertEquals(expected, alert)
    }

    @ParameterizedTest
    @MethodSource("provideDewPoint")
    fun dewPoint(temperature: Float, humidity: Float, expected: Float) {
        val dew = Meteorology.getDewPoint(temperature, humidity)
        assertEquals(expected, dew, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideLightningStrikes")
    fun lightningStrikes(lightning: Instant, thunder: Instant, expected: Float) {
        val distance = Meteorology.getLightningStrikeDistance(lightning, thunder)
        assertEquals(expected, distance, 0.5f)
    }

    @ParameterizedTest
    @MethodSource("provideLightningStrikeDistances")
    fun lightningStrikeDanger(distanceMeters: Float, expected: Boolean) {
        val danger = Meteorology.isLightningStrikeDangerous(Distance.meters(distanceMeters))
        assertEquals(expected, danger)
    }

    @ParameterizedTest
    @MethodSource("provideSeasons")
    fun seasons(expected: Season, isNorth: Boolean, date: LocalDate) {
        val season = Meteorology.getSeason(
            Coordinate(if (isNorth) 1.0 else -1.0, 0.0),
            ZonedDateTime.of(date, LocalTime.MIN, ZoneId.systemDefault())
        )
        assertEquals(expected, season)
    }

    @Test
    fun ambientTemperature() {
        val temp = Meteorology.getAmbientTemperature(170f, 125f, 100f)
        assertEquals(68.75f, temp)
    }

    @ParameterizedTest
    @MethodSource("provideSeaLevelPressure")
    fun convertsToSeaLevel(
        pressureHpa: Float,
        altitudeMeters: Float,
        temperatureCelsius: Float?,
        expectedPressureHpa: Float
    ) {
        val altitude = Distance.meters(altitudeMeters)
        val reading = Meteorology.getSeaLevelPressure(
            Pressure.hpa(pressureHpa),
            altitude,
            temperatureCelsius?.let { Temperature.celsius(it) })
        assertEquals(expectedPressureHpa, reading.hpa().value, 0.1f)
    }

    @ParameterizedTest
    @MethodSource("provideHighPressures")
    fun isHigh(pressureHpa: Float, isHigh: Boolean) {
        val ret = Meteorology.isHighPressure(Pressure.hpa(pressureHpa))
        assertEquals(isHigh, ret)
    }

    @ParameterizedTest
    @MethodSource("provideLowPressures")
    fun isLow(pressureHpa: Float, isLow: Boolean) {
        val ret = Meteorology.isLowPressure(Pressure.hpa(pressureHpa))
        assertEquals(isLow, ret)
    }

    @Test
    fun getKoppenGeigerClimateClassification() {
        val tests = listOf(
            //  Tropical
            Triple(
                "Af",
                monthlyTemperatures(26f, 26f, 26f, 26f, 26f, 25f, 25f, 26f, 26f, 26f, 26f, 26f),
                monthlyPrecipitation(
                    240.8f,
                    230f,
                    256.2f,
                    256.2f,
                    212f,
                    150.6f,
                    121.2f,
                    126.1f,
                    147.5f,
                    193.6f,
                    205.8f,
                    222.6f
                )
            ),
            Triple(
                "Am",
                monthlyTemperatures(27f, 27f, 27f, 27f, 27f, 27f, 27f, 28f, 28f, 28f, 28f, 28f),
                monthlyPrecipitation(
                    228.5f,
                    281.8f,
                    279.5f,
                    240.6f,
                    153.9f,
                    73.6f,
                    45.8f,
                    48f,
                    67.6f,
                    97.9f,
                    122.6f,
                    167.8f
                )
            ),
            Triple(
                "As",
                monthlyTemperatures(22f, 23f, 22f, 22f, 21f, 19f, 19f, 21f, 23f, 23f, 22f, 22f),
                monthlyPrecipitation(
                    203.2f,
                    178.9f,
                    168.5f,
                    82.3f,
                    20.2f,
                    6.2f,
                    3.1f,
                    9.8f,
                    34.6f,
                    103.3f,
                    185.6f,
                    220f
                )
            ),

            // Dry
            Triple(
                "BWh",
                monthlyTemperatures(12f, 15f, 20f, 25f, 30f, 32f, 32f, 31f, 30f, 24f, 18f, 13f),
                monthlyPrecipitation(1.8f, 1.8f, 2.4f, 0.7f, 1.5f, 1.7f, 1.2f, 1.2f, 0.9f, 1.9f, 1f, 1.3f)
            ),
            Triple(
                "BSk",
                monthlyTemperatures(2f, 4f, 9f, 14f, 19f, 24f, 26f, 25f, 21f, 14f, 8f, 2f),
                monthlyPrecipitation(
                    3.9f,
                    7.1f,
                    19.4f,
                    31.1f,
                    54.6f,
                    65.5f,
                    54f,
                    56f,
                    43.7f,
                    33f,
                    15.3f,
                    6.8f,
                    69.8f,
                    57.5f,
                    26.6f,
                    6.7f,
                    1.2f,
                    0f,
                    0f,
                    0f,
                    0f,
                    5.9f,
                    26.8f,
                    59.1f
                )
            ),

            // Temperate
            Triple(
                "Csa",
                monthlyTemperatures(10, 10, 11, 15, 19, 24, 26, 26, 22, 18, 14, 11),
                monthlyPrecipitation(84.8, 80.9, 55.1, 33.7, 15.9, 6.1, 0.8, 1, 10.5, 34.4, 75.2, 97.5)
            ),
            Triple(
                "Csb",
                monthlyTemperatures(5, 6, 8, 11, 14, 17, 20, 20, 17, 12, 8, 4),
                monthlyPrecipitation(
                    188.9,
                    167.6,
                    145.5,
                    100.6,
                    63.3,
                    41.1,
                    12.8,
                    13.6,
                    44.3,
                    114.1,
                    223.2,
                    221.9,
                    61.4,
                    27.5,
                    0.9,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0.1,
                    9.1,
                    51.9
                )
            ),
            Triple(
                "Cwb",
                monthlyTemperatures(10, 11, 13, 15, 16, 15, 14, 14, 14, 13, 11, 10),
                monthlyPrecipitation(7.8, 6.2, 4.6, 8, 31.8, 110.5, 125.6, 121.1, 111.2, 42.5, 7.6, 2.2)
            ),
            Triple(
                "Cfc",
                monthlyTemperatures(1, 1, 1, 3, 7, 10, 11, 11, 8, 5, 3, 1),
                monthlyPrecipitation(
                    99,
                    102,
                    88,
                    69,
                    56,
                    49,
                    59,
                    80,
                    106,
                    105,
                    103,
                    103,
                    214,
                    199,
                    154,
                    41,
                    2,
                    0,
                    0,
                    0,
                    0,
                    8,
                    61,
                    164
                )
            ),

            // Continental
            Triple(
                "Dsa",
                monthlyTemperatures(-1, 0, 5, 10, 15, 21, 25, 25, 20, 13, 6, 1),
                monthlyPrecipitation(
                    19,
                    23,
                    32,
                    35,
                    24,
                    8,
                    1,
                    1,
                    6,
                    27,
                    34,
                    29,
                    134,
                    123,
                    33,
                    6,
                    0,
                    0,
                    0,
                    0,
                    0,
                    2,
                    28,
                    71
                )
            ),
            Triple(
                "Dsb",
                monthlyTemperatures(-8, -7, -1, 6, 11, 16, 20, 20, 15, 8, 1, -5),
                monthlyPrecipitation(
                    4,
                    6,
                    23,
                    46,
                    43,
                    19,
                    8,
                    5,
                    12,
                    34,
                    31,
                    13,
                    310,
                    362,
                    187,
                    44,
                    1,
                    0,
                    0,
                    0,
                    0,
                    5,
                    70,
                    254
                )
            ),
            Triple(
                "Dwc",
                monthlyTemperatures(-23, -19, -9, 1, 9, 16, 18, 15, 8, -1, -12, -19),
                monthlyPrecipitation(0, 0, 0, 6, 25, 49, 72, 66, 33, 6, 0, 0, 3, 6, 18, 43, 15, 1, 0, 0, 4, 29, 23, 6)
            ),
            Triple(
                "Dfd",
                monthlyTemperatures(-39, -31, -26, -17, -6, 5, 12, 10, 4, -6, -19, -27),
                monthlyPrecipitation(
                    0,
                    0,
                    0,
                    0,
                    10,
                    38,
                    56,
                    53,
                    43,
                    13,
                    1,
                    0,
                    136,
                    131,
                    75,
                    168,
                    178,
                    46,
                    0,
                    0,
                    32,
                    134,
                    138,
                    83
                )
            ),

            // Polar
            Triple(
                "ET",
                monthlyTemperatures(-24, -25, -24, -16, -5, 2, 6, 4, -2, -10, -16, -21),
                monthlyPrecipitation(
                    0,
                    0,
                    0,
                    0,
                    3,
                    17,
                    38,
                    43,
                    16,
                    3,
                    1,
                    1,
                    23,
                    22,
                    17,
                    33,
                    34,
                    19,
                    2,
                    17,
                    82,
                    148,
                    78,
                    42
                )
            ),
            Triple(
                "EF",
                monthlyTemperatures(-24, -25, -24, -16, -5, -2, -1, -1, -2, -10, -16, -21),
                monthlyPrecipitation(
                    0,
                    0,
                    0,
                    0,
                    3,
                    17,
                    38,
                    43,
                    16,
                    3,
                    1,
                    1,
                    23,
                    22,
                    17,
                    33,
                    34,
                    19,
                    2,
                    17,
                    82,
                    148,
                    78,
                    42
                )
            ),
        )

        for ((classification, temperatures, precipitation) in tests) {
            val climate = Meteorology.getKoppenGeigerClimateClassification(
                temperatures,
                precipitation
            )
            assertEquals(classification, climate.code)
        }
    }

    companion object {

        fun monthlyTemperatures(
            jan: Number,
            feb: Number,
            mar: Number,
            apr: Number,
            may: Number,
            jun: Number,
            jul: Number,
            aug: Number,
            sep: Number,
            oct: Number,
            nov: Number,
            dec: Number
        ): Map<Month, Temperature> {
            return mapOf(
                Month.JANUARY to Temperature.celsius(jan.toFloat()),
                Month.FEBRUARY to Temperature.celsius(feb.toFloat()),
                Month.MARCH to Temperature.celsius(mar.toFloat()),
                Month.APRIL to Temperature.celsius(apr.toFloat()),
                Month.MAY to Temperature.celsius(may.toFloat()),
                Month.JUNE to Temperature.celsius(jun.toFloat()),
                Month.JULY to Temperature.celsius(jul.toFloat()),
                Month.AUGUST to Temperature.celsius(aug.toFloat()),
                Month.SEPTEMBER to Temperature.celsius(sep.toFloat()),
                Month.OCTOBER to Temperature.celsius(oct.toFloat()),
                Month.NOVEMBER to Temperature.celsius(nov.toFloat()),
                Month.DECEMBER to Temperature.celsius(dec.toFloat())
            )
        }

        fun monthlyPrecipitation(
            jan: Number,
            feb: Number,
            mar: Number,
            apr: Number,
            may: Number,
            jun: Number,
            jul: Number,
            aug: Number,
            sep: Number,
            oct: Number,
            nov: Number,
            dec: Number,
            janSnow: Number = 0,
            febSnow: Number = 0,
            marSnow: Number = 0,
            aprSnow: Number = 0,
            maySnow: Number = 0,
            junSnow: Number = 0,
            julSnow: Number = 0,
            augSnow: Number = 0,
            sepSnow: Number = 0,
            octSnow: Number = 0,
            novSnow: Number = 0,
            decSnow: Number = 0
        ): Map<Month, Distance> {
            return mapOf(
                Month.JANUARY to Distance.from(jan.toFloat() + janSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.FEBRUARY to Distance.from(feb.toFloat() + febSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.MARCH to Distance.from(mar.toFloat() + marSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.APRIL to Distance.from(apr.toFloat() + aprSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.MAY to Distance.from(may.toFloat() + maySnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.JUNE to Distance.from(jun.toFloat() + junSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.JULY to Distance.from(jul.toFloat() + julSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.AUGUST to Distance.from(aug.toFloat() + augSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.SEPTEMBER to Distance.from(sep.toFloat() + sepSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.OCTOBER to Distance.from(oct.toFloat() + octSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.NOVEMBER to Distance.from(nov.toFloat() + novSnow.toFloat() / 10, DistanceUnits.Millimeters),
                Month.DECEMBER to Distance.from(dec.toFloat() + decSnow.toFloat() / 10, DistanceUnits.Millimeters)
            )
        }

        @JvmStatic
        fun provideLowPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, true),
                Arguments.of(1009.144f, true),
                Arguments.of(1009.145f, false),
                Arguments.of(1013f, false),
                Arguments.of(1030f, false),
            )
        }

        @JvmStatic
        fun provideHighPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1000f, false),
                Arguments.of(1013f, false),
                Arguments.of(1022.688f, false),
                Arguments.of(1022.689f, true),
                Arguments.of(1030f, true),
            )
        }

        @JvmStatic
        fun provideSeaLevelPressure(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0f, 0f, null, 0f),
                Arguments.of(0f, 0f, 0f, 0f),
                Arguments.of(1000f, -100f, null, 988.2f),
                Arguments.of(980f, 200f, null, 1003.48f),
                Arguments.of(980f, 1000f, 15f, 1101.93f),
                Arguments.of(1000f, -100f, 28f, 988.71f),
            )
        }

        private val weatherTime = Instant.ofEpochSecond(100000)

        @JvmStatic
        fun provideWeatherForecasts(): Stream<Arguments> {
            return Stream.of(
                // Pressure only - storm (warm temps)
                Arguments.of(
                    pressures(1030f, 1021f),
                    emptyList<Reading<CloudGenus?>>(),
                    temperatures(10f, 20f),
                    weather(
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.FallingFast, -3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                        WeatherCondition.Rain
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // Pressure only - storm (cold temps)
                Arguments.of(
                    pressures(1030f, 1021f),
                    emptyList<Reading<CloudGenus?>>(),
                    temperatures(-10f, 0f),
                    weather(
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.FallingFast, -3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                        WeatherCondition.Snow
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // Pressure only - storm (slightly warm temps)
                Arguments.of(
                    pressures(1030f, 1021f),
                    emptyList<Reading<CloudGenus?>>(),
                    temperatures(-2f, 8f),
                    weather(
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.FallingFast, -3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // Pressure only - storm (slightly cold temps)
                Arguments.of(
                    pressures(1030f, 1021f),
                    emptyList<Reading<CloudGenus?>>(),
                    temperatures(-5f, 2f),
                    weather(
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.FallingFast, -3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // Pressure only - worsening slow
                Arguments.of(
                    pressures(1030f, 1027f),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        WeatherFront.Warm,
                        PressureSystem.High,
                        PressureTendency(PressureCharacteristic.Falling, -1f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Overcast,
                    ),
                    weatherLater(
                        null
                    )
                ),
                // Pressure only - worsening fast
                Arguments.of(
                    pressures(1009f, 1005f),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        WeatherFront.Warm,
                        PressureSystem.Low,
                        PressureTendency(PressureCharacteristic.FallingFast, -4 / 3f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),

                // Pressure only - improving slow
                Arguments.of(
                    pressures(1027f, 1030f),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        WeatherFront.Cold,
                        PressureSystem.High,
                        PressureTendency(PressureCharacteristic.Rising, 1f),
                        WeatherCondition.Clear
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // Pressure only - improving slow (not high pressure)
                Arguments.of(
                    pressures(1014f, 1017f),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        WeatherFront.Cold,
                        null,
                        PressureTendency(PressureCharacteristic.Rising, 1f)
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // Pressure only - improving fast
                Arguments.of(
                    pressures(1005f, 1009f),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weatherAt(
                        weatherTime,
                        WeatherFront.Cold,
                        PressureSystem.Low,
                        PressureTendency(PressureCharacteristic.RisingFast, 4 / 3f),
                        WeatherCondition.Wind
                    ),
                    weatherLater(
                        null
                    )
                ),

                // clouds only - warm front
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Stratus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // clouds only - warm front (Ns)
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Nimbostratus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1),
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // clouds only - cold front
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulus),
                    temperatures(10f, 20f),
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Cold,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Storm,
                        WeatherCondition.Thunderstorm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                        WeatherCondition.Rain
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // clouds only - cold front (Cb)
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulonimbus),
                    temperatures(10f, 20f),
                    weatherAt(
                        weatherTime.minusSeconds(1),
                        WeatherFront.Cold,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Storm,
                        WeatherCondition.Thunderstorm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                        WeatherCondition.Rain
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // clouds only - cold front (Cb only)
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cumulonimbus),
                    temperatures(10f, 20f),
                    weatherAt(
                        weatherTime.minusSeconds(1),
                        WeatherFront.Cold,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Storm,
                        WeatherCondition.Thunderstorm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast,
                        WeatherCondition.Rain
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // clouds only - cold front (Cb, cold temps)
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulonimbus),
                    temperatures(-10f, 10f),
                    weatherAt(
                        weatherTime.minusSeconds(1),
                        WeatherFront.Cold,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // clouds only - unknown front
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(12.0).minusMillis(500)),
                        null,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        null
                    )
                ),
                // clouds only - no front
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    clouds(CloudGenus.Cumulus, CloudGenus.Stratocumulus),
                    null,
                    weatherAt(
                        weatherTime,
                        null,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        null
                    )
                ),

                // no data
                Arguments.of(
                    emptyList<Reading<Pressure>>(),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        null,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f)
                    ),
                    weatherLater(
                        null
                    )
                ),

                // warm front (slow)
                Arguments.of(
                    pressures(1030f, 1027f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Stratus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Warm,
                        PressureSystem.High,
                        PressureTendency(PressureCharacteristic.Falling, -1f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        null
                    )
                ),
                // warm front (fast)
                Arguments.of(
                    pressures(1009f, 1005f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Stratus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Warm,
                        PressureSystem.Low,
                        PressureTendency(PressureCharacteristic.FallingFast, -4 / 3f),
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // warm front (storm)
                Arguments.of(
                    pressures(1030f, 1021f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Stratus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Warm,
                        null,
                        PressureTendency(PressureCharacteristic.FallingFast, -3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),
                // cold front (slow)
                Arguments.of(
                    pressures(1030f, 1027f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Cold,
                        PressureSystem.High,
                        PressureTendency(PressureCharacteristic.Falling, -1f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),
                // cold front (fast)
                Arguments.of(
                    pressures(1009f, 1005f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Cold,
                        PressureSystem.Low,
                        PressureTendency(PressureCharacteristic.FallingFast, -4 / 3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        null
                    )
                ),
                // cold front (storm)
                Arguments.of(
                    pressures(1030f, 1021f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulus),
                    null,
                    weatherAt(
                        weatherTime.minusSeconds(1).plus(Time.hours(1.5)),
                        WeatherFront.Cold,
                        null,
                        PressureTendency(PressureCharacteristic.FallingFast, -3f),
                        WeatherCondition.Storm,
                        WeatherCondition.Precipitation,
                        WeatherCondition.Wind,
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),

                // Cold front (passing)
                Arguments.of(
                    pressures(1027f, 1030f),
                    clouds(CloudGenus.Cirrus, CloudGenus.Altocumulus, CloudGenus.Cumulus),
                    null,
                    weather(
                        WeatherFront.Cold,
                        PressureSystem.High,
                        PressureTendency(PressureCharacteristic.Rising, 1f),
                        WeatherCondition.Clear
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),

                // Overcast in high pressure
                Arguments.of(
                    pressures(1030f, 1030f),
                    clouds(CloudGenus.Stratus),
                    null,
                    weatherAt(
                        weatherTime,
                        null,
                        PressureSystem.High,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Overcast
                    ),
                    weatherLater(
                        PressureSystem.High,
                        WeatherCondition.Clear
                    )
                ),

                // Clear in low pressure
                Arguments.of(
                    pressures(1000f, 1000f),
                    clouds(null),
                    null,
                    weatherAt(
                        weatherTime,
                        null,
                        PressureSystem.Low,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Clear
                    ),
                    weatherLater(
                        PressureSystem.Low,
                        WeatherCondition.Overcast
                    )
                ),

                // Unknown
                Arguments.of(
                    pressures(1022f, 1022f),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        null,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f)
                    ),
                    weatherLater(null)
                ),

                // Replaces unknown forecast (using pressure)
                Arguments.of(
                    pressures(
                        1024f,
                        1023f
                    ).map { it.copy(time = it.time.minus(Duration.ofHours(6))) } + pressures(
                        1022f,
                        1022f
                    ),
                    emptyList<Reading<CloudGenus?>>(),
                    null,
                    weather(
                        null,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Clear
                    ),
                    weatherLater(null)
                ),

                // Replaces unknown forecast (using clouds)
                Arguments.of(
                    pressures(
                        1022f,
                        1022f
                    ),
                    clouds(CloudGenus.Stratus).map { it.copy(time = it.time.minus(Duration.ofHours(6))) },
                    null,
                    weather(
                        null,
                        null,
                        PressureTendency(PressureCharacteristic.Steady, 0f),
                        WeatherCondition.Overcast
                    ),
                    weatherLater(null)
                )
            )
        }

        private fun temperatures(low: Float, high: Float): Range<Temperature> {
            return Range(Temperature.celsius(low), Temperature.celsius(high))
        }

        private fun weatherLater(
            system: PressureSystem?,
            vararg conditions: WeatherCondition
        ): WeatherForecast {
            return WeatherForecast(
                null,
                conditions.toList(),
                null,
                system,
                null
            )
        }

        private fun weatherAt(
            time: Instant,
            front: WeatherFront?,
            system: PressureSystem?,
            tendency: PressureTendency,
            vararg conditions: WeatherCondition
        ): WeatherForecast {
            return WeatherForecast(
                time,
                conditions.toList(),
                front,
                system,
                tendency
            )
        }

        private fun weather(
            front: WeatherFront?,
            system: PressureSystem?,
            tendency: PressureTendency,
            vararg conditions: WeatherCondition
        ): WeatherForecast {
            return WeatherForecast(
                null,
                conditions.toList(),
                front,
                system,
                tendency
            )
        }

        private fun pressures(last: Float, now: Float): List<Reading<Pressure>> {
            val time = weatherTime
            return listOf(
                Reading(Pressure.hpa(last), time.minus(Duration.ofHours(3))),
                Reading(Pressure.hpa(now), time)
            )
        }

        private fun clouds(vararg clouds: CloudGenus?): List<Reading<CloudGenus?>> {
            var time = weatherTime.minusSeconds(clouds.size.toLong())
            return clouds.map {
                val r = Reading(it, time)
                time = time.plusSeconds(1)
                r
            }
        }

        @JvmStatic
        fun provideForecasts(): Stream<Arguments> {
            // First argument not used yet
            return Stream.of(
                Arguments.of(PressureCharacteristic.Steady, 0f, -6f, Weather.NoChange),
                Arguments.of(PressureCharacteristic.FallingFast, -6f, -6f, Weather.Storm),
                Arguments.of(PressureCharacteristic.FallingFast, -8f, -7f, Weather.Storm),
                Arguments.of(PressureCharacteristic.FallingFast, -6 / 3f, null, Weather.Storm),
                Arguments.of(
                    PressureCharacteristic.FallingFast,
                    -5 / 3f,
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
                    1000f,
                    1000f,
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    1000f,
                    1001f,
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Steady, 1 / 3f)
                ),
                Arguments.of(
                    1000f,
                    1004f,
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.RisingFast, 4 / 3f)
                ),
                Arguments.of(
                    1000f,
                    1003f,
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Rising, 1f)
                ),
                Arguments.of(
                    1004f,
                    1000f,
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -4 / 3f)
                ),
                Arguments.of(
                    1002f,
                    1000f,
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Falling, -2 / 3f)
                ),
                Arguments.of(
                    1002f,
                    1000f,
                    Duration.ofHours(3),
                    1 / 3f,
                    PressureTendency(PressureCharacteristic.Falling, -2 / 3f)
                ),
                Arguments.of(
                    1003f,
                    1000f,
                    Duration.ofHours(3),
                    1 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -1f)
                ),
                Arguments.of(
                    1002f,
                    1000f,
                    Duration.ofHours(2),
                    1 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -1f)
                ),
                Arguments.of(
                    1008f,
                    1000f,
                    Duration.ofHours(4),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -2f)
                ),
                Arguments.of(
                    1000f,
                    1000f,
                    Duration.ZERO,
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    1000.1f,
                    1000f,
                    Duration.ZERO,
                    2 / 3f,
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
                Arguments.of(Distance.kilometers(10f).meters().value, true),
                Arguments.of(Distance.kilometers(10.1f).meters().value, false),
                Arguments.of(10000f, true),
                Arguments.of(100f, true),
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