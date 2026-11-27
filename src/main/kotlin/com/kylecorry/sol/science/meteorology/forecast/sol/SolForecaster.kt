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
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.PrecipitationLaterPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.PrecipitationPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.StormPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.ThunderstormPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.WeatherPredictor
import com.kylecorry.sol.science.meteorology.forecast.sol.predictors.WindPredictor
import com.kylecorry.sol.science.meteorology.observation.WeatherObservation
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature
import java.time.Duration
import java.time.Instant

internal object SolForecaster : Forecaster {
    private val SKY_PERSISTENCE_MAX = Duration.ofHours(8)
    private val HISTORY_DURATION_MAX = Duration.ofHours(48)
    private val CLOUDS_UP_TO_DATE_DURATION = Duration.ofHours(24)
    private val SKY_CONDITIONS = setOf(WeatherCondition.Clear, WeatherCondition.Overcast)
    private val lowPressureLaterPredictor = LowPressureLaterPredictor()
    private val highPressureLaterPredictor = HighPressureLaterPredictor()

    private val weatherPredictors = listOf(
        WeatherCondition.Storm to StormPredictor(),
        WeatherCondition.Thunderstorm to ThunderstormPredictor(),
        WeatherCondition.Precipitation to PrecipitationPredictor(),
        WeatherCondition.Wind to WindPredictor(),
        WeatherCondition.Clear to ClearPredictor(),
        WeatherCondition.Overcast to OvercastPredicator()
    )

    private val afterWeatherPredictors = listOf(
        WeatherCondition.Precipitation to PrecipitationLaterPredictor(),
        WeatherCondition.Clear to ClearLaterPredictor(),
        WeatherCondition.Overcast to OvercastLaterPredictor()
    )

    private data class Inputs(
        val pressures: List<Reading<Pressure>>,
        val clouds: List<Reading<CloudGenus?>>,
        val dailyTemperatureRange: Range<Temperature>?,
        val thresholds: SolForecastThresholds
    )

    private class Prediction(
        val now: WeatherForecast,
        val after: WeatherForecast,
        val factors: SolForecastFactors,
        val clouds: List<Reading<CloudGenus?>>
    )

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
            Inputs(
                pressures,
                clouds,
                dailyTemperatureRange,
                SolForecastThresholds(pressureChangeThreshold, pressureStormChangeThreshold)
            ),
            time
        )
    }

    private fun forecast(inputs: Inputs, time: Instant): List<WeatherForecast> {
        val prediction = predict(inputs, time)
        val now = if (prediction.now.conditions.any { it in SKY_CONDITIONS }) {
            prediction.now
        } else {
            val sky = getRecentSky(inputs, time)
            prediction.now.copy(conditions = prediction.now.conditions + listOfNotNull(sky))
        }

        val arrivalTime = ArrivalTimeCalculator.getArrivalTime(
            time,
            now,
            prediction.clouds,
            prediction.factors.doCloudsIndicateWeatherFront
        )

        return listOf(now.copy(time = arrivalTime), prediction.after)
    }

    private fun getRecentSky(inputs: Inputs, time: Instant): WeatherCondition? {
        val earliest = time.minus(SKY_PERSISTENCE_MAX)
        val pressureOnlyInputs = inputs.copy(clouds = emptyList())
        return inputs.pressures
            .asSequence()
            .map { it.time }
            .filter { it in earliest..<time }
            .distinct()
            .sortedDescending()
            .firstNotNullOfOrNull { at ->
                predict(pressureOnlyInputs, at).now.conditions.firstOrNull { it in SKY_CONDITIONS }
            }
    }

    private fun predict(inputs: Inputs, time: Instant): Prediction {
        val oldest = time.minus(HISTORY_DURATION_MAX)
        val cloudsUpToDateTime = time.minus(CLOUDS_UP_TO_DATE_DURATION)
        val filteredPressures = inputs.pressures
            .filter { it.time in oldest..time }
            .sortedBy { it.time }
        var filteredClouds = inputs.clouds
            .filter { it.time in oldest..time }
            .sortedBy { it.time }

        // Don't look at clouds if none were logged in the last 24 hours
        if (filteredClouds.none { it.time >= cloudsUpToDateTime }) {
            filteredClouds = emptyList()
        }

        val factors = SolForecastFactorCalculator.calculate(
            filteredPressures,
            filteredClouds,
            inputs.thresholds,
            inputs.dailyTemperatureRange,
            time
        )

        val conditions = weatherPredictors.filter { it.second.isLikely(factors) }.map { it.first }
        val afterConditions = afterWeatherPredictors.filter { it.second.isLikely(factors) }.map { it.first }

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
            ForecastHelper.addSecondaryConditions(conditions, inputs.dailyTemperatureRange),
            factors.weatherFront,
            factors.pressureSystem,
            factors.pressureTendency
        )
        val after = WeatherForecast(
            null,
            ForecastHelper.addSecondaryConditions(afterConditions, inputs.dailyTemperatureRange),
            null,
            afterSystem
        )

        return Prediction(now, after, factors, filteredClouds)
    }
}
