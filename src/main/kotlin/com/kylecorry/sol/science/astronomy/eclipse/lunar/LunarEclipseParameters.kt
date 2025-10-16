package com.kylecorry.sol.science.astronomy.eclipse.lunar

import kotlinx.datetime.Instant

internal data class LunarEclipseParameters(
    val maximum: Instant,
    val minDistanceFromCenter: Double,
    val umbralConeRadius: Double,
    val n: Double
)