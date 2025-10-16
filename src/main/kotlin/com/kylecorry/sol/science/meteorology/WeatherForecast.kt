package com.kylecorry.sol.science.meteorology

import kotlinx.datetime.Instant

data class WeatherForecast(
    val time: Instant?,
    val conditions: List<WeatherCondition>,
    val front: WeatherFront? = null,
    val system: PressureSystem? = null,
    val tendency: PressureTendency? = null
)