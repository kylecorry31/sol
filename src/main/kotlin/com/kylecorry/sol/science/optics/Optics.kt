package com.kylecorry.sol.science.optics

import com.kylecorry.sol.math.SolMath.power
import com.kylecorry.sol.math.SolMath.tanDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity
import com.kylecorry.sol.units.meters
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

object Optics {

    /**
     * Calculates the angular size of an object
     * @param diameter The diameter of the object
     * @param distance The distance to the object
     * @return The angular size in degrees
     */
    fun getAngularSize(diameter: Quantity<Distance>, distance: Quantity<Distance>): Float {
        return getAngularSize(diameter.meters().amount, distance.meters().amount)
    }

    /**
     * Calculates the angular size of an object
     * @param diameter The diameter of the object in any unit
     * @param distance The distance to the object in the same unit
     * @return The angular size in degrees
     */
    fun getAngularSize(diameter: Float, distance: Float): Float {
        return (2 * atan2(diameter / 2f, distance)).toDegrees()
    }

    /**
     * Calculates the perspective projection of a point onto the camera's image plane
     * @param point The point to project in the camera's coordinate system
     * @param focalLength The focal length (in pixels)
     * @param opticalCenter The optical center (in pixels)
     * @return The projected point in pixels
     */
    fun perspectiveProjection(
        point: Vector3,
        focalLength: Vector2,
        opticalCenter: Vector2,
    ): Vector2 {
        val fx = focalLength.x
        val fy = focalLength.y
        val cx = opticalCenter.x
        val cy = opticalCenter.y
        return Vector2(
            fx * point.x / point.z + cx,
            fy * point.y / point.z + cy
        )
    }

    /**
     * Calculates the inverse perspective projection of a point on the camera's image plane
     * @param point The point on the image plane in pixels
     * @param focalLength The focal length (in pixels)
     * @param opticalCenter The optical center (in pixels)
     * @param distance The distance to the point
     * @return The point in the camera's coordinate system
     */
    fun inversePerspectiveProjection(
        point: Vector2,
        focalLength: Vector2,
        opticalCenter: Vector2,
        distance: Float = 1f
    ): Vector3 {
        val fx = focalLength.x
        val fy = focalLength.y
        val cx = opticalCenter.x
        val cy = opticalCenter.y
        // Z is unknown, so we need to solve for it
        val cx2 = cx * cx
        val cy2 = cy * cy
        val fx2 = fx * fx
        val fy2 = fy * fy
        val x2 = point.x * point.x
        val y2 = point.y * point.y

        var z = distance * fx * fy * sqrt(
            1 / (cx2 * fy2 - 2 * cx * fy2 * point.x + cy2 * fx2 - 2 * cy * fx2 * point.y + fx2 * fy2 + fx2 * y2 + fy2 * x2)
        )
        // This can be plus or minus, but given it is visible, it is probably positive
        if (z < 0) {
            z *= -1
        }
        val x = (point.x - cx) * z / fx
        val y = (point.y - cy) * z / fy
        return Vector3(x, y, z)
    }

    /**
     * Calculates the focal length in pixels
     * @param focalLength The focal length in a physical unit
     * @param sensorSize The sensor size in the same physical unit
     * @param sensorSizePixels The sensor size in pixels
     * @return The focal length in pixels
     */
    fun getFocalLengthPixels(
        focalLength: Float,
        sensorSize: Float,
        sensorSizePixels: Int
    ): Float {
        return focalLength * sensorSizePixels / sensorSize
    }

    /**
     * Calculates the field of view in degrees. Assumes the lens is rectilinear.
     * @param focalLength The focal length in any unit
     * @param sensorSize The sensor size in the same unit
     */
    fun getFieldOfView(
        focalLength: Float,
        sensorSize: Float
    ): Float {
        // https://www.bobatkins.com/photography/technical/field_of_view.html
        return 2 * atan(sensorSize / (2 * focalLength)).toDegrees()
    }

    /**
     * Calculates the focal length given the field of view. Assumes the lens is rectilinear.
     * @param fieldOfView The field of view in degrees
     * @param viewSize The view size
     * @return The focal length in the same unit as viewSize
     */
    fun getFocalLength(fieldOfView: Float, viewSize: Float): Float {
        return viewSize / (2 * tanDegrees(fieldOfView / 2f))
    }

    /**
     * Corrects the distortion of a point using the Brown-Conrady distortion model.
     * @param point The point on the original image
     * @param opticalCenter The optical center of the image, defaults to the center of the image
     * @param radialDistortionCoefficients The radial distortion coefficients
     * @param tangentialDistortionCoefficients The tangential distortion coefficients
     * @return The undistorted point
     */
    fun brownConradyDistortionCorrection(
        point: Vector2,
        opticalCenter: Vector2,
        radialDistortionCoefficients: List<Float>,
        tangentialDistortionCoefficients: List<Float> = emptyList(),
    ): Vector2 {
        // https://en.wikipedia.org/wiki/Distortion_(optics)
        val cx = opticalCenter.x
        val cy = opticalCenter.y

        val normalizedX = point.x - cx
        val normalizedY = point.y - cy

        val rSquared = normalizedX * normalizedX + normalizedY * normalizedY

        var radialDistortion = 1f

        for (i in radialDistortionCoefficients.indices) {
            radialDistortion += radialDistortionCoefficients[i] * power(rSquared, i + 1)
        }

        var sharedTangentialDistortion = 1f
        for (i in 2..<tangentialDistortionCoefficients.size) {
            sharedTangentialDistortion += tangentialDistortionCoefficients[i] * power(rSquared, i - 1)
        }

        val p1 = tangentialDistortionCoefficients.getOrNull(0) ?: 0f
        val p2 = tangentialDistortionCoefficients.getOrNull(1) ?: 0f

        val xCorrection =
            normalizedX * radialDistortion + (p1 * (rSquared + 2 * normalizedX * normalizedX) + 2 * p2 * normalizedX * normalizedY) * sharedTangentialDistortion
        val yCorrection =
            normalizedY * radialDistortion + (2 * p1 * normalizedX * normalizedY + p2 * (rSquared + 2 * normalizedY * normalizedY)) * sharedTangentialDistortion

        return Vector2(point.x + xCorrection, point.y + yCorrection)
    }

    fun luxToCandela(lux: Float, distance: Quantity<Distance>): Float {
        val meters = distance.meters().amount
        return lux * meters * meters
    }

    fun luxAtDistance(candela: Float, distance: Quantity<Distance>): Float {
        val meters = distance.meters().amount
        return candela / (meters * meters)
    }

    fun lightBeamDistance(candela: Float): Quantity<Distance> {
        return Distance.meters(sqrt(candela * 4))
    }

}