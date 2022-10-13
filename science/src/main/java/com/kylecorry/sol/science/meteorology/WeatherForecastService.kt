package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.CloudService
import com.kylecorry.sol.units.Reading

internal object WeatherForecastService {

    fun getWeather(front: WeatherFront): WeatherCondition {
        val clouds = when (front) {
            WeatherFront.Warm -> listOf(CloudGenus.Nimbostratus)
            WeatherFront.Cold -> listOf(CloudGenus.Cumulonimbus)
        }

        val precipitation = clouds.flatMap { Meteorology.getPrecipitation(it) }

        return WeatherCondition(
            precipitation,
            clouds,
            true
        )
    }

    fun getWeather(system: PressureSystem): WeatherCondition {
        val clouds = when (system) {
            PressureSystem.Low -> listOf(CloudGenus.Stratus, CloudGenus.Stratocumulus)
            PressureSystem.High -> listOf(CloudGenus.Cumulus, null)
        }

        return WeatherCondition(
            emptyList(),
            clouds,
            false
        )
    }

    fun getNextPressureSystem(front: WeatherFront): PressureSystem {
        return when (front) {
            WeatherFront.Warm -> PressureSystem.Low
            WeatherFront.Cold -> PressureSystem.High
        }
    }

    fun getFront(tendency: PressureTendency): WeatherFront? {
        return if (tendency.characteristic.isFalling) {
            WeatherFront.Warm
        } else if (tendency.characteristic.isRising) {
            WeatherFront.Cold
        } else {
            null
        }
    }

    fun getFront(clouds: List<Reading<CloudGenus?>>): WeatherFront? {
        // TODO: Warm = Cirro-Alto-St/Ns
        // TODO: Cold = Cirro-Alto-Cu/Cb
        return null
    }

}