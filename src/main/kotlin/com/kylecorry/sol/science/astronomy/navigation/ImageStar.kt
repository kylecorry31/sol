package com.kylecorry.sol.science.astronomy.navigation

/**
 * A star in image pixel coordinates (origin top left, pixel centers at +0.5)
 * @param brightness only the relative brightness matters, the brightest stars are matched first
 */
data class ImageStar(val x: Double, val y: Double, val brightness: Double)
