package com.kylecorry.sol.science.astronomy.navigation

/**
 * @param maxMagnitude the faintest catalog stars to match against
 * @param seedMagnitude the faintest catalog stars used to form hypotheses (retries with maxMagnitude)
 * @param seedCount the number of brightest image stars used to form hypotheses
 * @param pairToleranceDegrees the fixed part of the pair separation tolerance
 * @param focalUncertainty the fractional uncertainty of the camera's focal length
 * @param inlierToleranceDegrees the maximum distance between a star and the catalog to count as a match
 * @param maxZenithOffsetDegrees hypotheses that put gravity's up further than this from the zenith
 * at the approximate location are rejected
 */
data class StarIdentifierSettings(
    val maxMagnitude: Float = 4.5f,
    val seedMagnitude: Float = 3.0f,
    val includePlanets: Boolean = true,
    val seedCount: Int = 10,
    val pairToleranceDegrees: Double = 0.25,
    val focalUncertainty: Double = 0.06,
    val inlierToleranceDegrees: Double = 0.35,
    val maxZenithOffsetDegrees: Double = 30.0,
    val minMatches: Int = 4,
    val fitFocalLength: Boolean = true,
    val fitDistortion: Boolean = true,
    val withRefraction: Boolean = true
)
