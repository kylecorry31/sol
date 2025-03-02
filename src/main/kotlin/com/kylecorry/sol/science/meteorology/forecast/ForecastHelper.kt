package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.*
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Quantity
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature
import java.time.Duration

internal object ForecastHelper {
    fun addSecondaryConditions(
        conditions: List<WeatherCondition>,
        dailyTemperatureRange: Range<Temperature>? = null,
    ): List<WeatherCondition> {
        val mutableConditions = conditions.toMutableList()

        // Storms should always be paired with precipitation and wind
        if (mutableConditions.contains(WeatherCondition.Storm)) {
            mutableConditions.add(WeatherCondition.Precipitation)
            mutableConditions.add(WeatherCondition.Wind)
        }

        // Precipitation should always be paired with overcast
        if (mutableConditions.contains(WeatherCondition.Precipitation)) {
            mutableConditions.add(WeatherCondition.Overcast)

            // Try to infer the precipitation type
            val precipitationType = getPrecipitationType(dailyTemperatureRange)
            if (precipitationType != null) {
                mutableConditions.add(precipitationType)
            }
        }

        return mutableConditions.distinct()
    }

    fun getPressureSystem(pressure: Quantity<Pressure>): PressureSystem? {
        return if (Meteorology.isHighPressure(pressure)) {
            PressureSystem.High
        } else if (Meteorology.isLowPressure(pressure)) {
            PressureSystem.Low
        } else {
            null
        }
    }

    fun getTendency(
        pressures: List<Reading<Quantity<Pressure>>>,
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
}