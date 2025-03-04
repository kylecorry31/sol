package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.shared.Season
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
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
        pressures: List<Reading<Quantity<Pressure>>>,
        clouds: List<Reading<CloudGenus?>>,
        temperatures: Range<Quantity<Temperature>>?,
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
        last: Quantity<Pressure>,
        current: Quantity<Pressure>,
        duration: Duration,
        threshold: Float,
        expectedTendency: PressureTendency
    ) {
        val tendency = Meteorology.getTendency(last, current, duration, threshold)
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
    fun lightningStrikeDanger(distance: Quantity<Distance>, expected: Boolean) {
        val danger = Meteorology.isLightningStrikeDangerous(distance)
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
        pressure: Quantity<Pressure>,
        altitude: Quantity<Distance>,
        temperature: Quantity<Temperature>?,
        expected: Quantity<Pressure>
    ) {
        val reading = Meteorology.getSeaLevelPressure(pressure, altitude, temperature)
        assertEquals(expected.amount, reading.amount, 0.1f)
        assertEquals(expected.units, reading.units)
    }

    @ParameterizedTest
    @MethodSource("provideHighPressures")
    fun isHigh(pressure: Quantity<Pressure>, isHigh: Boolean) {
        val ret = Meteorology.isHighPressure(pressure)
        assertEquals(isHigh, ret)
    }

    @ParameterizedTest
    @MethodSource("provideLowPressures")
    fun isLow(pressure: Quantity<Pressure>, isLow: Boolean) {
        val ret = Meteorology.isLowPressure(pressure)
        assertEquals(isLow, ret)
    }


    companion object {

        @JvmStatic
        fun provideLowPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Quantity(1000f, Pressure.Hpa), true),
                Arguments.of(Quantity(1009.144f, Pressure.Hpa), true),
                Arguments.of(Quantity(1009.145f, Pressure.Hpa), false),
                Arguments.of(Quantity(1013f, Pressure.Hpa), false),
                Arguments.of(Quantity(1030f, Pressure.Hpa), false),
            )
        }

        @JvmStatic
        fun provideHighPressures(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Quantity(1000f, Pressure.Hpa), false),
                Arguments.of(Quantity(1013f, Pressure.Hpa), false),
                Arguments.of(Quantity(1022.688f, Pressure.Hpa), false),
                Arguments.of(Quantity(1022.689f, Pressure.Hpa), true),
                Arguments.of(Quantity(1030f, Pressure.Hpa), true),
            )
        }

        @JvmStatic
        fun provideSeaLevelPressure(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Quantity(0f, Pressure.Hpa),
                    Distance.meters(0f),
                    null,
                    Quantity(0f, Pressure.Hpa)
                ),
                Arguments.of(
                    Quantity(0f, Pressure.Hpa),
                    Distance.meters(0f),
                    Quantity(0f, Temperature.Celsius),
                    Quantity(0f, Pressure.Hpa)
                ),
                Arguments.of(
                    Quantity(1000f, Pressure.Hpa),
                    Distance.meters(-100f),
                    null,
                    Quantity(988.2f, Pressure.Hpa)
                ),
                Arguments.of(
                    Quantity(980f, Pressure.Hpa),
                    Distance.meters(200f),
                    null,
                    Quantity(1003.48f, Pressure.Hpa)
                ),
                Arguments.of(
                    Quantity(980f, Pressure.Hpa),
                    Distance.meters(1000f),
                    Quantity(15f, Temperature.Celsius),
                    Quantity(1101.93f, Pressure.Hpa)
                ),
                Arguments.of(
                    Quantity(1000f, Pressure.Hpa),
                    Distance.meters(-100f),
                    Quantity(28f, Temperature.Celsius),
                    Quantity(988.71f, Pressure.Hpa)
                ),
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

        private fun temperatures(low: Float, high: Float): Range<Quantity<Temperature>> {
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

        private fun pressures(last: Float, now: Float): List<Reading<Quantity<Pressure>>> {
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
                    Quantity(1000f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(3),
                    2f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    Quantity(1000f, Pressure.Hpa),
                    Quantity(1001f, Pressure.Hpa),
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Steady, 1 / 3f)
                ),
                Arguments.of(
                    Quantity(1000f, Pressure.Hpa),
                    Quantity(1004f, Pressure.Hpa),
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.RisingFast, 4 / 3f)
                ),
                Arguments.of(
                    Quantity(1000f, Pressure.Hpa),
                    Quantity(1003f, Pressure.Hpa),
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Rising, 1f)
                ),
                Arguments.of(
                    Quantity(1004f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -4 / 3f)
                ),
                Arguments.of(
                    Quantity(1002f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(3),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Falling, -2 / 3f)
                ),
                Arguments.of(
                    Quantity(1002f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(3),
                    1 / 3f,
                    PressureTendency(PressureCharacteristic.Falling, -2 / 3f)
                ),
                Arguments.of(
                    Quantity(1003f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(3),
                    1 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -1f)
                ),
                Arguments.of(
                    Quantity(1002f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(2),
                    1 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -1f)
                ),
                Arguments.of(
                    Quantity(1008f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ofHours(4),
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.FallingFast, -2f)
                ),
                Arguments.of(
                    Quantity(1000f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
                    Duration.ZERO,
                    2 / 3f,
                    PressureTendency(PressureCharacteristic.Steady, 0f)
                ),
                Arguments.of(
                    Quantity(1000.1f, Pressure.Hpa),
                    Quantity(1000f, Pressure.Hpa),
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