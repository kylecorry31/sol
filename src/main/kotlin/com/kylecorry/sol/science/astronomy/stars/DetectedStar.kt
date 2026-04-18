package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.science.astronomy.units.CelestialObservation

data class DetectedStar(val star: Star, val reading: CelestialObservation, val confidence: Float)
