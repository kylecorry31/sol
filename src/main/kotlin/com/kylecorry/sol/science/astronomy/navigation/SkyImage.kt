package com.kylecorry.sol.science.astronomy.navigation

import com.kylecorry.sol.math.Vector3Precise
import com.kylecorry.sol.science.optics.PinholeCamera
import java.time.ZonedDateTime

/**
 * A photo of the sky
 * @param camera the camera model, the focal length and distortion are refined during identification
 * @param up gravity's up direction in the camera's coordinate system, or null if unknown
 */
data class SkyImage(
    val stars: List<ImageStar>,
    val camera: PinholeCamera,
    val up: Vector3Precise?,
    val time: ZonedDateTime
)
