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

internal object SolForecastFactorCalculator {
    private val cloudPrecipitationCalculator = CloudPrecipitationCalculator()

    fun calculate(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        pressureChangeThreshold: Float,
        dailyTemperatureRange: Range<Temperature>?,
        time: Instant
    ): SolForecastFactors {
        val tendency = ForecastHelper.getTendency(pressures, pressureChangeThreshold)
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
        return SolForecastFactors(
            tendency,
            system,
            doCloudsIndicateWeatherFront,
            cloudFront,
            front,
            dailyTemperatureRange,
            lastCloud
        )
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
