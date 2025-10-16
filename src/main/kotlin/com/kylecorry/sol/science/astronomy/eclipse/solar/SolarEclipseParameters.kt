package com.kylecorry.sol.science.astronomy.eclipse.solar

import kotlinx.datetime.Instant

internal data class SolarEclipseParameters(
    val maximum: Instant,
    val minDistanceFromCenter: Double,
    val umbralConeRadius: Double
)