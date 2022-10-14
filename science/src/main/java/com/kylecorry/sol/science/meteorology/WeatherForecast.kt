package com.kylecorry.sol.science.meteorology

import java.time.Instant

data class WeatherForecast(
    val time: Instant,
    val conditions: List<WeatherCondition>,
    val front: WeatherFront? = null,
    val system: PressureSystem? = null
)