package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Distance

data class CelestialObservation(
    val azimuth: Bearing,
    val altitude: Float,
    val angularDiameter: Float? = null,
    val visualMagnitude: Float? = null,
    val distance: Distance? = null
)