package com.kylecorry.trailsensecore.science.astronomy.eclipse

import java.time.Instant

data class LunarEclipseParameters(
    val maximum: Instant,
    val minDistanceFromCenter: Double,
    val umbralConeRadius: Double,
    val n: Double
)