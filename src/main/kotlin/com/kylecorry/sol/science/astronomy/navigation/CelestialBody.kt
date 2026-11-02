package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.science.astronomy.locators.Planet
import com.kylecorry.sol.science.astronomy.stars.Star

/**
 * A star or planet that can be identified in a photo
 */
data class CelestialBody(val name: String, val magnitude: Float, val star: Star?, val planet: Planet?)
