package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.science.meteorology.clouds.CloudGenus

data class WeatherCondition(
    val precipitation: List<Precipitation>,
    val clouds: List<CloudGenus?>,
    val isWindy: Boolean // TODO: Eventually estimate the wind speed
)