package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.geometry.Rotation
import com.kylecorry.sol.science.optics.PinholeCamera

/**
 * @param camera the camera model with the fitted focal length and distortion
 * @param rotation maps camera rays to East-North-Up vectors at the location used for identification
 * @param rmsDegrees the RMS angular error between the identified stars and the catalog
 */
data class StarIdentification(
    val stars: List<IdentifiedStar>,
    val camera: PinholeCamera,
    val rotation: Rotation,
    val rmsDegrees: Double
)
