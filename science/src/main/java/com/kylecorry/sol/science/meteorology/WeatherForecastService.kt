package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.CloudGenusMatcher
import com.kylecorry.sol.science.meteorology.clouds.CloudMatcher
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.time.Time.middle
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.TemperatureUnits
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue

internal object WeatherForecastService {

    private val thunderstormMinTemperature = Temperature(55f, TemperatureUnits.F).celsius()

    private fun getTendency(
        pressures: List<Reading<Pressure>>,
        threshold: Float
    ): PressureTendency {
        if (pressures.size < 2) {
            return PressureTendency(PressureCharacteristic.Steady, 0f)
        }

        val pressure = pressures.last()

        val targetTime = pressure.time.minus(Duration.ofHours(3))
        val lastPressure = pressures.minBy {
            if (it == pressure) {
                Long.MAX_VALUE
            } else {
                Duration.between(
                    it.time,
                    targetTime
                ).abs().seconds
            }
        }
        return Meteorology.getTendency(
            lastPressure.value,
            pressure.value,
            Duration.between(lastPressure.time, pressure.time),
            threshold
        )
    }

    private fun getPressureSystem(pressures: List<Reading<Pressure>>): PressureSystem? {
        if (pressures.isEmpty()) {
            return null
        }
        val pressure = pressures.last()
        return if (Meteorology.isHighPressure(pressure.value)) {
            PressureSystem.High
        } else if (Meteorology.isLowPressure(pressure.value)) {
            PressureSystem.Low
        } else {
            null
        }
    }

    private fun doRecentCloudsMatchPattern(
        clouds: List<Reading<CloudGenus?>>,
        pattern: List<CloudMatcher>
    ): Boolean {
        var idx = pattern.lastIndex
        var hasHit = false
        var cloudIdx = clouds.lastIndex
        while (cloudIdx >= 0 && idx >= 0) {
            val cloud = clouds[cloudIdx].value
            if (pattern[idx].matches(cloud)) {
                hasHit = true
                cloudIdx--
            } else if (!hasHit) {
                return false
            } else {
                hasHit = false
                idx--
            }
        }
        return idx == -1 || (idx == 0 && hasHit)
    }

    private fun doCloudsIndicateFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        val cirro = listOf(CloudGenus.Cirrus, CloudGenus.Cirrocumulus, CloudGenus.Cirrostratus)
        val alto = listOf(CloudGenus.Altocumulus, CloudGenus.Altostratus)
        val warm = listOf(CloudGenus.Stratus, CloudGenus.Nimbostratus)
        val cold = listOf(CloudGenus.Cumulus, CloudGenus.Cumulonimbus)
        val storm = listOf(CloudGenus.Nimbostratus, CloudGenus.Cumulonimbus)
        // TODO: Maybe detect very start of storm (cirro) and middle (alto) without further evidence
        val patterns = listOf(
            listOf(CloudGenusMatcher(cirro), CloudGenusMatcher(alto)),
            listOf(CloudGenusMatcher(cirro), CloudGenusMatcher(alto), CloudGenusMatcher(warm)),
            listOf(CloudGenusMatcher(cirro), CloudGenusMatcher(alto), CloudGenusMatcher(cold)),
            listOf(CloudGenusMatcher(storm)),
        )
        return patterns.any { doRecentCloudsMatchPattern(clouds, it) }
    }

    private fun doCloudsIndicateColdFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        val cirro = listOf(CloudGenus.Cirrus, CloudGenus.Cirrocumulus, CloudGenus.Cirrostratus)
        val alto = listOf(CloudGenus.Altocumulus, CloudGenus.Altostratus)
        val cold = listOf(CloudGenus.Cumulus, CloudGenus.Cumulonimbus)
        val storm = listOf(CloudGenus.Cumulonimbus)
        val patterns = listOf(
            listOf(CloudGenusMatcher(cirro), CloudGenusMatcher(alto), CloudGenusMatcher(cold)),
            listOf(CloudGenusMatcher(storm)),
        )
        return patterns.any { doRecentCloudsMatchPattern(clouds, it) }
    }

    private fun doCloudsIndicateWarmFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        val cirro = listOf(CloudGenus.Cirrus, CloudGenus.Cirrocumulus, CloudGenus.Cirrostratus)
        val alto = listOf(CloudGenus.Altocumulus, CloudGenus.Altostratus)
        val warm = listOf(CloudGenus.Stratus, CloudGenus.Nimbostratus)
        val storm = listOf(CloudGenus.Nimbostratus)
        val patterns = listOf(
            listOf(CloudGenusMatcher(cirro), CloudGenusMatcher(alto), CloudGenusMatcher(warm)),
            listOf(CloudGenusMatcher(storm)),
        )
        return patterns.any { doRecentCloudsMatchPattern(clouds, it) }
    }

    private fun getCurrentCloudConditions(
        clouds: List<Reading<CloudGenus?>>,
        time: Instant
    ): WeatherCondition? {
        val last = clouds.lastOrNull() ?: return null
        if (Duration.between(last.time, time).abs() > Duration.ofHours(3)) {
            return null
        }
        if (last.value == null) {
            return WeatherCondition.Clear
        }

        val overcastClouds = listOf(
            CloudGenus.Stratus,
            CloudGenus.Nimbostratus,
            CloudGenus.Stratocumulus,
            CloudGenus.Altostratus
        )
        if (overcastClouds.contains(last.value)) {
            return WeatherCondition.Overcast
        }

        return null
    }

    fun forecast(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        dailyTemperatureRange: Range<Temperature>? = null,
        time: Instant = Instant.now(),
        pressureChangeThreshold: Float = 1.5f,
        pressureStormChangeThreshold: Float = 2f
    ): List<WeatherForecast> {
        val forecast = forecastHelper(
            pressures,
            clouds,
            dailyTemperatureRange,
            time,
            pressureChangeThreshold,
            pressureStormChangeThreshold
        )

        // There are current conditions, so just return the forecast
        if (forecast.first().conditions.isNotEmpty()) {
            return forecast
        }

        // Try to figure out what the current conditions are based on past predictions
        var startTime = getNextStartTime(time, pressures, clouds)
        val maxTime = time.minus(noChangeMaxHistory)
        while (startTime != null && startTime.isAfter(maxTime)) {
            val previous = forecastHelper(
                pressures,
                clouds,
                dailyTemperatureRange,
                startTime,
                pressureChangeThreshold,
                pressureStormChangeThreshold
            )

            // Get the conditions of the previous prediction, starting with the furthest out prediction
            val conditions =
                previous.reversed().firstOrNull { it.conditions.isNotEmpty() }?.conditions
                    ?: emptyList()

            if (conditions.isNotEmpty()) {
                return forecast.withCurrentConditions(conditions)
            }

            val newTime = getNextStartTime(startTime, pressures, clouds)
            // Prevents an infinite loop (shouldn't be possible, but just in case)
            if (startTime == newTime) {
                break
            }
            startTime = newTime
        }

        return forecast
    }

    private fun getNextStartTime(
        time: Instant,
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>
    ): Instant? {
        val times = pressures.map { it.time } + clouds.map { it.time }
        return Time.getClosestPastTime(time, times)
    }

    private fun List<WeatherForecast>.withCurrentConditions(conditions: List<WeatherCondition>): List<WeatherForecast> {
        return mapIndexed { index, value ->
            if (index == 0) {
                value.copy(conditions = conditions)
            } else {
                value
            }
        }
    }

    private fun forecastHelper(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        dailyTemperatureRange: Range<Temperature>? = null,
        time: Instant = Instant.now(),
        pressureChangeThreshold: Float = 1.5f,
        pressureStormChangeThreshold: Float = 2f
    ): List<WeatherForecast> {
        val history = Duration.ofHours(48)
        val cloudsUpToDateDuration = Duration.ofHours(24)
        val oldest = time.minus(history)
        val cloudsUpToDateTime = time.minus(cloudsUpToDateDuration)
        val filteredPressures =
            pressures.filter { it.time <= time && it.time >= oldest }.sortedBy { it.time }
        var filteredClouds =
            clouds.filter { it.time <= time && it.time >= oldest }.sortedBy { it.time }

        // Don't look at clouds if none were logged in the last 24 hours
        if (filteredClouds.none { it.time >= cloudsUpToDateTime }) {
            filteredClouds = emptyList()
        }
        val tendency = getTendency(filteredPressures, pressureChangeThreshold)
        val system = getPressureSystem(filteredPressures)
        val cloudFront = doCloudsIndicateFront(filteredClouds)
        val cloudWarmFront = doCloudsIndicateWarmFront(filteredClouds)
        val cloudColdFront = doCloudsIndicateColdFront(filteredClouds)

        val conditions = mutableListOf<WeatherCondition>()
        val afterConditions = mutableListOf<WeatherCondition>()

        val isColdFront = cloudColdFront || tendency.characteristic.isRising
        val isWarmFront = !isColdFront && (tendency.characteristic.isFalling || cloudWarmFront)

        if (!tendency.characteristic.isRising && (cloudColdFront || (tendency.characteristic.isFalling && tendency.amount.absoluteValue >= pressureStormChangeThreshold.absoluteValue))) {
            conditions.add(WeatherCondition.Storm)
            val temp = dailyTemperatureRange?.end?.celsius() ?: Temperature.zero
            if (isColdFront && temp > thunderstormMinTemperature) {
                conditions.add(WeatherCondition.Thunderstorm)
            }
        }

        if ((isWarmFront || cloudFront) && !tendency.characteristic.isRising) {
            conditions.add(WeatherCondition.Precipitation)
        }

        if (tendency.characteristic.isRapid || (cloudColdFront && !tendency.characteristic.isRising)) {
            conditions.add(WeatherCondition.Wind)
        }

        val currentCloudConditions = getCurrentCloudConditions(filteredClouds, time)

        if (currentCloudConditions == null && system == PressureSystem.High && (tendency.characteristic == PressureCharacteristic.Steady || tendency.characteristic.isRising)) {
            conditions.add(WeatherCondition.Clear)
        }

        if (currentCloudConditions == null && system == PressureSystem.Low && (tendency.characteristic == PressureCharacteristic.Steady || tendency.characteristic.isFalling)) {
            conditions.add(WeatherCondition.Overcast)
        }

        if (currentCloudConditions != null) {
            conditions.add(currentCloudConditions)
        }

        // After
        if ((isColdFront && system != PressureSystem.Low) || (currentCloudConditions != null && system == PressureSystem.High && tendency.characteristic == PressureCharacteristic.Steady)) {
            afterConditions.add(WeatherCondition.Clear)
        }

        if (isWarmFront && system != PressureSystem.High || (currentCloudConditions != null && system == PressureSystem.Low && tendency.characteristic == PressureCharacteristic.Steady)) {
            afterConditions.add(WeatherCondition.Overcast)
        }

        val front = if (isColdFront) {
            WeatherFront.Cold
        } else if (isWarmFront) {
            WeatherFront.Warm
        } else {
            null
        }

        val afterSystem =
            if ((isColdFront && system != PressureSystem.Low) || (system == PressureSystem.High && tendency.characteristic == PressureCharacteristic.Steady)) {
                PressureSystem.High
            } else if ((isWarmFront && system != PressureSystem.High) || (system == PressureSystem.Low && tendency.characteristic == PressureCharacteristic.Steady)) {
                PressureSystem.Low
            } else {
                null
            }

        if (conditions.contains(WeatherCondition.Precipitation)) {
            getPrecipitationType(dailyTemperatureRange)?.let { conditions.add(it) }
        }

        if (afterConditions.contains(WeatherCondition.Precipitation)) {
            getPrecipitationType(dailyTemperatureRange)?.let { afterConditions.add(it) }
        }

        // TODO: Now, soon, and later buckets (or predict next X hours)
        val now = WeatherForecast(null, conditions.distinct(), front, system, tendency)
        val after = WeatherForecast(null, afterConditions.distinct(), null, afterSystem)

        val arrivalTime = getArrivalTime(time, now, filteredClouds)

        return listOf(now.copy(time = arrivalTime), after)
    }

    private fun getArrivalTime(
        time: Instant,
        forecast: WeatherForecast,
        clouds: List<Reading<CloudGenus?>>
    ): Instant? {

        val hasSkyCondition =
            forecast.conditions.contains(WeatherCondition.Clear) || forecast.conditions.contains(
                WeatherCondition.Overcast
            )
        val isSteady = forecast.tendency?.characteristic == PressureCharacteristic.Steady
        val hasSingleCondition = forecast.conditions.size == 1
        val hasWindCondition = forecast.conditions.contains(WeatherCondition.Wind)

        if (hasSingleCondition && ((hasSkyCondition && isSteady) || hasWindCondition)) {
            return time
        }

        // Can't currently predict the time for other conditions
        if (!forecast.conditions.contains(WeatherCondition.Precipitation)) {
            return null
        }

        val cloudIndicatedArrivals = clouds.reversed().map { cloud ->
            val duration = getCloudPrecipitationTimeRange(cloud.value)
            duration?.let { Range(cloud.time.plus(it.start), cloud.time.plus(it.end)) }
        }

        val cloudIndicatedArrivalRange = Range.intersection(
            cloudIndicatedArrivals.filterNotNull(),
            stopWhenNoIntersection = true
        )

        return cloudIndicatedArrivalRange?.middle()
    }

    private fun getCloudPrecipitationTimeRange(cloud: CloudGenus?): Range<Duration>? {
        return when (cloud) {
            CloudGenus.Cirrus -> Range(Duration.ofHours(12), Duration.ofHours(24))
            CloudGenus.Cirrocumulus -> Range(Duration.ofHours(8), Duration.ofHours(12))
            CloudGenus.Cirrostratus -> Range(Duration.ofHours(10), Duration.ofHours(15))
            CloudGenus.Altocumulus -> Range(Duration.ZERO, Duration.ofHours(12))
            CloudGenus.Altostratus -> Range(Duration.ZERO, Duration.ofHours(8))
            CloudGenus.Nimbostratus, CloudGenus.Cumulonimbus -> Range(Duration.ZERO, Duration.ZERO)
            CloudGenus.Stratus, CloudGenus.Cumulus -> Range(Duration.ZERO, Duration.ofHours(3))
            CloudGenus.Stratocumulus, null -> null
        }
    }

    private fun getPrecipitationType(temperatures: Range<Temperature>?): WeatherCondition? {
        temperatures ?: return null

        if (temperatures.start.celsius().temperature > 0) {
            return WeatherCondition.Rain
        }

        if (temperatures.end.celsius().temperature <= 0) {
            return WeatherCondition.Snow
        }

        return null
    }

    private val noChangeMaxHistory = Duration.ofHours(8)

}