package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.SolMath.clamp
import com.kylecorry.sol.math.statistics.StatisticsService
import com.kylecorry.sol.units.Reading
import java.time.Duration

class CloudCoverageWeatherForecaster : IWeatherForecaster<Float> {

    private val statistics = StatisticsService()

    override fun forecast(readings: List<Reading<Float>>): Float {
        val tendency = getTendency(readings)
        // Cloud coverage increasing means worse weather
        return -clamp(tendency, -1f, 1f)
    }

    private fun getTendency(readings: List<Reading<Float>>): Float {
        val first = readings.firstOrNull() ?: return 0f
        val normalizedReadings = readings.map {
            val hours = Duration.between(first.time, it.time).seconds / 3600f
            hours to it.value
        }
        return statistics.slope(normalizedReadings)
    }

}