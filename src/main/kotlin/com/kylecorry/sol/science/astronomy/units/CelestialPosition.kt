package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Distance

data class CelestialPosition(
    val azimuth: Bearing,
    val altitude: Float,
    val angularDiameter: Float,
    val distance: Distance? = null
)