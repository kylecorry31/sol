package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue

internal object WeatherForecastService {

    private fun getTendency(
        pressures: List<Reading<Pressure>>,
        threshold: Float
    ): PressureTendency {
        val lastPressure = pressures.last() // TODO: This is not correct
        val pressure = pressures.last()
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

    private fun doCloudsIndicateFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        TODO("Implement this")
    }

    private fun doCloudsIndicateColdFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        TODO("Implement this")
    }

    private fun doCloudsIndicateWarmFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        TODO("Implement this")
    }

    fun forecast(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        time: Instant = Instant.now(),
        pressureChangeThreshold: Float = 1.5f,
        pressureStormChangeThreshold: Float = 2f
    ): List<WeatherForecast> {
        val history = Duration.ofHours(24)
        val oldest = time.minus(history)
        val filteredPressures =
            pressures.filter { it.time <= time && it.time >= oldest }.sortedBy { it.time }
        val filteredClouds =
            clouds.filter { it.time <= time && it.time >= oldest }.sortedBy { it.time }
        val tendency = getTendency(filteredPressures, pressureChangeThreshold)
        val system = getPressureSystem(filteredPressures)
        val cloudFront = doCloudsIndicateFront(filteredClouds)
        val cloudWarmFront = doCloudsIndicateWarmFront(filteredClouds)
        val cloudColdFront = doCloudsIndicateColdFront(filteredClouds)

        val conditions = mutableListOf<WeatherCondition>()
        val afterConditions = mutableListOf<WeatherCondition>()

        val isColdFront = cloudColdFront || tendency.characteristic.isRising
        val isWarmFront = !isColdFront && (tendency.characteristic.isFalling || cloudWarmFront)

        if (cloudColdFront || (tendency.characteristic.isFalling && tendency.amount.absoluteValue >= pressureStormChangeThreshold.absoluteValue)) {
            conditions.add(WeatherCondition.Storm)
        }

        if ((isWarmFront || cloudFront) && !tendency.characteristic.isRising) {
            conditions.add(WeatherCondition.Precipitation)
        }

        if (tendency.characteristic.isRapid) {
            conditions.add(WeatherCondition.Wind)
        }

        if (system == PressureSystem.High && tendency.characteristic == PressureCharacteristic.Steady) {
            conditions.add(WeatherCondition.Clear)
        }

        if (system == PressureSystem.Low && tendency.characteristic == PressureCharacteristic.Steady) {
            conditions.add(WeatherCondition.Overcast)
        }

        // After
        if (isColdFront && system != PressureSystem.Low) {
            afterConditions.add(WeatherCondition.Clear)
        }

        if (isWarmFront && system != PressureSystem.High) {
            afterConditions.add(WeatherCondition.Overcast)
        }

        // TODO: Determine time from clouds / rate of change
        val timeAfter = time.plus(Duration.ofHours(4))

        // TODO: Now, soon, and later buckets (or predict next X hours)
        val now = WeatherForecast(time, conditions)
        val after = WeatherForecast(timeAfter, afterConditions)

        return listOf(now, after)
    }

}