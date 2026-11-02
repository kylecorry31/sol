package com.kylecorry.sol.science.optics

import com.kylecorry.sol.math.Vector3Precise
import kotlin.math.tan

/**
 * A pinhole camera with a single radial distortion coefficient.
 *
 * Pixels have their origin in the top left with y pointing down. Rays are in the camera's
 * coordinate system: x right, y down, and the camera looks down +z.
 *
 * @param focalLength the focal length in pixels
 * @param k1 the radial distortion coefficient (distorted = undistorted * (1 + k1 * r^2))
 */
data class PinholeCamera(
    val width: Int,
    val height: Int,
    val focalLength: Double,
    val k1: Double = 0.0,
    val centerX: Double = width / 2.0,
    val centerY: Double = height / 2.0
) {

    fun toRay(x: Double, y: Double): Vector3Precise {
        val xd = (x - centerX) / focalLength
        val yd = (y - centerY) / focalLength
        var xu = xd
        var yu = yd
        repeat(UNDISTORT_ITERATIONS) {
            val scale = 1 + k1 * (xu * xu + yu * yu)
            xu = xd / scale
            yu = yd / scale
        }
        return Vector3Precise(xu, yu, 1.0).normalize()
    }

    /**
     * @return the pixel or null if the ray is behind the camera
     */
    fun toPixel(ray: Vector3Precise): Pair<Double, Double>? {
        if (ray.z <= 0) {
            return null
        }
        val xu = ray.x / ray.z
        val yu = ray.y / ray.z
        val scale = 1 + k1 * (xu * xu + yu * yu)
        return (centerX + focalLength * xu * scale) to (centerY + focalLength * yu * scale)
    }

    companion object {
        private const val UNDISTORT_ITERATIONS = 6

        fun fromVerticalFieldOfView(width: Int, height: Int, degrees: Double): PinholeCamera {
            val f = (height / 2.0) / tan(Math.toRadians(degrees / 2))
            return PinholeCamera(width, height, f)
        }
    }
}
