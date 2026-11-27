package com.kylecorry.sol.science.meteorology.forecast.sol

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.clouds.CloudPrecipitationCalculator
import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

/**
 * @param pressureChange the change (hPa / hr) considered a change in the weather
 * @param pressureStormChange the change (hPa / hr) considered a storm
 */
internal data class SolForecastThresholds(val pressureChange: Float, val pressureStormChange: Float)

internal object SolForecastFactorCalculator {
    private val cloudPrecipitationCalculator = CloudPrecipitationCalculator()
    private val MINIMUM_HISTORY = Duration.ofHours(2)
    private val RANGE_DURATION = Duration.ofHours(48)
    private const val DEFAULT_THREE_HOUR_CHANGE = 1.5f

    fun calculate(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        thresholds: SolForecastThresholds,
        dailyTemperatureRange: Range<Temperature>?,
        time: Instant
    ): SolForecastFactors {
        val tendency = ForecastHelper.getTendency(pressures, thresholds.pressureChange)
        val system = getPressureSystem(pressures)
        val doCloudsIndicateWeatherFront = doCloudsIndicateFront(clouds)
        val cloudWarmFront = doCloudsIndicateWarmFront(clouds)
        val cloudColdFront = doCloudsIndicateColdFront(clouds)
        val isColdFront = cloudColdFront || tendency.characteristic.isRising
        val isWarmFront = !isColdFront && (tendency.characteristic.isFalling || cloudWarmFront)
        val cloudFront = if (cloudColdFront) {
            WeatherFront.Cold
        } else if (cloudWarmFront) {
            WeatherFront.Warm
        } else {
            null
        }
        val front = if (isColdFront) {
            WeatherFront.Cold
        } else if (isWarmFront) {
            WeatherFront.Warm
        } else {
            null
        }
        val lastCloud = clouds.lastOrNull()?.takeIf { Duration.between(it.time, time).abs() <= Duration.ofHours(3) }
        val change3h = getChange(pressures, Duration.ofHours(3))
        val change12h = getChange(pressures, Duration.ofHours(12)) ?: change3h
        val change24h = getChange(pressures, Duration.ofHours(24)) ?: change12h
        val recentPressures = pressures
            .filter { it.time >= time.minus(RANGE_DURATION) }
            .map { it.value.hpa().value }
        return SolForecastFactors(
            tendency,
            system,
            doCloudsIndicateWeatherFront,
            cloudFront,
            front,
            thresholds.pressureChange * 3 / DEFAULT_THREE_HOUR_CHANGE,
            abs(thresholds.pressureStormChange) * 3,
            pressures.lastOrNull()?.value?.hpa()?.value,
            change3h,
            change12h,
            change24h,
            if (recentPressures.isEmpty()) null else recentPressures.max() - recentPressures.min(),
            dailyTemperatureRange,
            lastCloud
        )
    }

    private fun getChange(pressures: List<Reading<Pressure>>, window: Duration): Float? {
        val latest = pressures.lastOrNull() ?: return null
        val past = getPastReading(pressures, latest, window)
        return past?.let { latest.value.hpa().value - it.value.hpa().value }
    }

    private fun getPastReading(
        pressures: List<Reading<Pressure>>,
        latest: Reading<Pressure>,
        window: Duration
    ): Reading<Pressure>? {
        val target = latest.time.minus(window)
        val oldest = pressures.first()
        return if (oldest.time.isAfter(target)) {
            oldest.takeIf { Duration.between(it.time, latest.time) >= MINIMUM_HISTORY }
        } else {
            pressures.minBy { abs(Duration.between(it.time, target).seconds) }
        }
    }

    private fun getPressureSystem(pressures: List<Reading<Pressure>>): PressureSystem? {
        if (pressures.isEmpty()) {
            return null
        }
        val pressure = pressures.last()
        return ForecastHelper.getPressureSystem(pressure.value)
    }

    private fun doCloudsIndicateFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        val patterns = CloudPrecipitationCalculator.frontPatterns
        return patterns.any {
            cloudPrecipitationCalculator.getMatch(clouds, it, true) != null
        }
    }

    private fun doCloudsIndicateColdFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        val patterns = CloudPrecipitationCalculator.coldFrontPatterns
        return patterns.any {
            cloudPrecipitationCalculator.getMatch(clouds, it, true) != null
        }
    }

    private fun doCloudsIndicateWarmFront(clouds: List<Reading<CloudGenus?>>): Boolean {
        val patterns = CloudPrecipitationCalculator.warmFrontPatterns
        return patterns.any {
            cloudPrecipitationCalculator.getMatch(clouds, it, true) != null
        }
    }
}
