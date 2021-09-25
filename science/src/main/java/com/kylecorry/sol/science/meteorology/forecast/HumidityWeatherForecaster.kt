package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.units.Reading

class HumidityWeatherForecaster : IWeatherForecaster<Float> {

    override fun forecast(readings: List<Reading<Float>>): Float {
        val last = readings.lastOrNull() ?: return 0f

        if (last.value == 0f || last.value.isNaN()) {
            return 0f
        }

        return -((last.value * 2) - 1)
    }
}