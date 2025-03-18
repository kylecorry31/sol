package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Utils
import com.kylecorry.sol.units.Bearing
import kotlin.math.atan2

// From https://stackoverflow.com/questions/16317599/android-compass-that-can-compensate-for-tilt-and-pitch

internal object AzimuthCalculator {

    fun calculate(gravity: FloatArray, magneticField: FloatArray): Bearing? {
        // East vector - perpendicular to gravity and magnetic field
        val east = Vector3Utils.normalize(Vector3Utils.cross(magneticField, gravity), true)

        // North vector - projection of magnetic field onto the ground plane
        val north = Vector3Utils.normalize(Vector3Utils.cross(gravity, east), true)

        // Azimuth
        // See https://math.stackexchange.com/questions/381649/whats-the-best-3d-angular-co-ordinate-system-for-working-with-smartfone-apps
        // North and East are perpendicular and are rotated around the down vector
        // Therefore their orientation compared to the forward facing vector is the azimuth (acting as sine/cosine on the unit circle)
        // Sine = east.x + north.y
        // Cosine = east.y - north.x (north vector is flipped around the y axis when compared to east)

        val sin = east[0] + north[1]
        val cos = east[1] - north[0]
        val azimuth = if (!(SolMath.isZero(cos) && SolMath.isApproximatelyEqual(cos, sin))) atan2(cos, sin) else 0f

        if (azimuth.isNaN()){
            return null
        }

        return Bearing(azimuth.toDegrees())
    }


    fun calculate(gravity: Vector3, magneticField: Vector3): Bearing? {
       return calculate(gravity.toFloatArray(), magneticField.toFloatArray())
    }

}