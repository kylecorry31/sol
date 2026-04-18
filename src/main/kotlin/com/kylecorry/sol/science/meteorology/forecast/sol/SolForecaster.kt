package com.kylecorry.sol.science.meteorology.forecast.sol

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.WeatherCondition
import com.kylecorry.sol.science.meteorology.WeatherForecast
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.science.meteorology.forecast.ForecastHelper
import com.kylecorry.sol.science.meteorology.forecast.Forecaster
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.ClearLaterPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.ClearPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.HighPressureLaterPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.LowPressureLaterPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.OvercastLaterPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.OvercastPredicator
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.PrecipitationPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.StormPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.ThunderstormPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.WeatherPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.WindPredictor
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.time.Time
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature
import java.time.Duration
import java.time.Instant

internal object SolForecaster : Forecaster {
    private val HISTORY_DURATION_NO_CHANGE_MAX = Duration.ofHours(8)
    private val HISTORY_DURATION_MAX = Duration.ofHours(48)
    private val CLOUDS_UP_TO_DATE_DURATION = Duration.ofHours(24)
    private val lowPressureLaterPredictor = LowPressureLaterPredictor()
    private val highPressureLaterPredictor = HighPressureLaterPredictor()

    override fun forecast(
        observations: List<WeatherObservation<*>>,
        dailyTemperatureRange: Range<Temperature>?,
        time: Instant,
        pressureChangeThreshold: Float,
        pressureStormChangeThreshold: Float,
        location: Coordinate
    ): List<WeatherForecast> {
        require(pressureChangeThreshold > 0) { "Pressure change threshold must be greater than 0." }
        require(pressureStormChangeThreshold > 0) { "Pressure storm change threshold must be greater than 0." }

        val pressures = observations
            .filterIsInstance<WeatherObservation.Pressure>()
            .map { it.asReading() }

        val clouds = observations
            .filterIsInstance<WeatherObservation.CloudGenus>()
            .map { it.asReading() }

        return forecast(
            pressures,
            clouds,
            dailyTemperatureRange,
            time,
            pressureChangeThreshold,
            pressureStormChangeThreshold
        )
    }

    private fun forecast(
        pressures: List<Reading<Pressure>>,
        clouds: List<Reading<CloudGenus?>>,
        dailyTemperatureRange: Range<Temperature>? = null,
        time: Instant = Instant.now(),
        pressureChangeThreshold: Float = 1.5f,
        pressureStormChangeThreshold: Float = 2f
    ): List<WeatherForecast> {
        val weatherPredictors = listOf(
            WeatherCondition.Storm to StormPredictor(pressureStormChangeThreshold),
            WeatherCondition.Thunderstorm to ThunderstormPredictor(pressureStormChangeThreshold),
            WeatherCondition.Precipitation to PrecipitationPredictor(),
            WeatherCondition.Wind to WindPredictor(),
            WeatherCondition.Clear to ClearPredictor(),
            WeatherCondition.Overcast to OvercastPredicator()
        )

        val afterWeatherPredictors = listOf(
            WeatherCondition.Clear to ClearLaterPredictor(),
            WeatherCondition.Overcast to OvercastLaterPredictor()
        )

        val forecast = forecastHelper(
            pressures,
            clouds,
            dailyTemperatureRange,
            time,
            pressureChangeThreshold,
            weatherPredictors,
            afterWeatherPredictors
        )

        // There are current conditions, so just return the forecast
        if (forecast.first().conditions.isNotEmpty()) {
            return forecast
        }

        // Try to figure out what the current conditions are based on past predictions
        val times = pressures.map { it.time } + clouds.map { it.time }
        var startTime = Time.getClosestPastTime(time, times)
        val minTime = time.minus(HISTORY_DURATION_NO_CHANGE_MAX)
        val maxIterations = times.size
        var i = 0
        while (startTime != null && startTime.isAfter(minTime) && i < maxIterations) {
            val previous = forecastHelper(
                pressures,
                clouds,
                dailyTemperatureRange,
                startTime,
                pressureChangeThreshold,
                weatherPredictors,
                afterWeatherPredictors
            )

            // Get the conditions of the previous prediction, starting with the furthest out prediction
            val conditions = previous
                .reversed()
                .firstOrNull { it.conditions.isNotEmpty() }
                ?.conditions ?: emptyList()

            if (conditions.isNotEmpty()) {
                return forecast.withCurrentConditions(conditions)
            }

            val newTime = Time.getClosestPastTime(startTime, times)
            check(startTime != newTime)
            startTime = newTime
            i++
        }

        return forecast
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
        weatherPredictors: List<Pair<WeatherCondition, WeatherPredictor>>,
        afterWeatherPredictors: List<Pair<WeatherCondition, WeatherPredictor>>,
    ): List<WeatherForecast> {
        val oldest = time.minus(HISTORY_DURATION_MAX)
        val cloudsUpToDateTime = time.minus(CLOUDS_UP_TO_DATE_DURATION)
        val filteredPressures = pressures
            .filter { it.time in oldest..time }
            .sortedBy { it.time }
        var filteredClouds = clouds
            .filter { it.time in oldest..time }
            .sortedBy { it.time }

        // Don't look at clouds if none were logged in the last 24 hours
        if (filteredClouds.none { it.time >= cloudsUpToDateTime }) {
            filteredClouds = emptyList()
        }

        // Forecast factors
        val factors = SolForecastFactorCalculator.calculate(
            filteredPressures,
            filteredClouds,
            pressureChangeThreshold,
            dailyTemperatureRange,
            time
        )

        val conditions = mutableListOf<WeatherCondition>()
        val afterConditions = mutableListOf<WeatherCondition>()

        for (predictor in weatherPredictors) {
            if (predictor.second.isLikely(factors)) {
                conditions.add(predictor.first)
            }
        }

        // After
        for (predictor in afterWeatherPredictors) {
            if (predictor.second.isLikely(factors)) {
                afterConditions.add(predictor.first)
            }
        }

        val afterSystem =
            if (highPressureLaterPredictor.isLikely(factors)) {
                PressureSystem.High
            } else if (lowPressureLaterPredictor.isLikely(factors)) {
                PressureSystem.Low
            } else {
                null
            }

        val now = WeatherForecast(
            null,
            ForecastHelper.addSecondaryConditions(conditions, dailyTemperatureRange),
            factors.weatherFront,
            factors.pressureSystem,
            factors.pressureTendency
        )
        val after = WeatherForecast(
            null,
            ForecastHelper.addSecondaryConditions(afterConditions, dailyTemperatureRange),
            null,
            afterSystem
        )

        val arrivalTime = ArrivalTimeCalculator.getArrivalTime(time, now, filteredClouds)

        return listOf(now.copy(time = arrivalTime), after)
    }
}
