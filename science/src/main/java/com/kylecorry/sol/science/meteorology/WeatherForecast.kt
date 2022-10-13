package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import java.time.Instant

data class WeatherForecast(
    val time: Instant,
    val clouds: List<CloudGenus>
)