package com.kylecorry.sol.science.meteorology

import java.time.Instant

data class WeatherForecast(
    val time: Instant,
    val conditions: List<WeatherCondition>
)