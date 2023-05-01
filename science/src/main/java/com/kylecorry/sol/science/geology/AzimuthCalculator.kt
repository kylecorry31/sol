package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Utils
import com.kylecorry.sol.units.Bearing
import java.util.Vector
import kotlin.math.atan2

// From https://stackoverflow.com/questions/16317599/android-compass-that-can-compensate-for-tilt-and-pitch

internal object AzimuthCalculator {

    fun calculate(gravity: FloatArray, magneticField: FloatArray): Bearing? {
        // Gravity
        val normGravity = Vector3Utils.normalize(gravity)
        val normMagField = Vector3Utils.normalize(magneticField)

        // East vector
        val east = Vector3Utils.cross(normMagField, normGravity)
        val normEast = Vector3Utils.normalize(east)

        // North vector
        val north = Vector3Utils.cross(normGravity, normEast)
        val normNorth = Vector3Utils.normalize(north)

        // Azimuth
        // NB: see https://math.stackexchange.com/questions/381649/whats-the-best-3d-angular-co-ordinate-system-for-working-with-smartfone-apps
        val sin = normEast[1] - normNorth[0]
        val cos = normEast[0] + normNorth[1]
        val azimuth = if (!(sin == 0f && sin == cos)) atan2(sin, cos) else 0f

        if (azimuth.isNaN()) {
            return null
        }

        return Bearing(azimuth.toDegrees())
    }


    fun calculate(gravity: Vector3, magneticField: Vector3): Bearing? {
        return calculate(gravity.toFloatArray(), magneticField.toFloatArray())
    }

}