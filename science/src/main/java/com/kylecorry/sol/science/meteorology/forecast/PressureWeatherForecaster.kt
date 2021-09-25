package com.kylecorry.sol.science.meteorology.forecast

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.clamp
import com.kylecorry.sol.math.SolMath.norm
import com.kylecorry.sol.math.statistics.StatisticsService
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import java.time.Duration
import kotlin.math.absoluteValue

class PressureWeatherForecaster(
    private val stormThreshold: Float = -2f,
) : IWeatherForecaster<Pressure> {

    private val statistics = StatisticsService()

    override fun forecast(readings: List<Reading<Pressure>>): Float {
        val tendency = getTendency(readings)
        return clamp(
            norm(
                tendency,
                -stormThreshold.absoluteValue,
                stormThreshold.absoluteValue
            ), -1f, 1f
        )
    }

    private fun getTendency(readings: List<Reading<Pressure>>): Float {
        val first = readings.firstOrNull() ?: return 0f
        val normalizedReadings = readings.map {
            val hours = Duration.between(first.time, it.time).seconds / 3600f
            hours to it.value.hpa().pressure
        }
        return statistics.slope(normalizedReadings)
    }

}