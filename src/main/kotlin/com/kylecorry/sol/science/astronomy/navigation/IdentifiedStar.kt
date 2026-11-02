package com.kylecorry.sol.science.astronomy.navigation

/**
 * @param index the index of the star in SkyImage.stars
 */
data class IdentifiedStar(val index: Int, val star: ImageStar, val body: CelestialBody)
