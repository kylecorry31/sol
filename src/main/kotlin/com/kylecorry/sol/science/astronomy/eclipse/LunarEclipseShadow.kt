package com.kylecorry.sol.science.astronomy.eclipse

import com.kylecorry.sol.science.astronomy.units.CelestialObservation

data class LunarEclipseShadow(
    val umbra: CelestialObservation,
    val penumbra: CelestialObservation
)
