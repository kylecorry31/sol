package com.kylecorry.sol.science.geology

import android.hardware.SensorManager
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Utils
import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.units.Bearing
import kotlin.math.atan2

// From https://stackoverflow.com/questions/16317599/android-compass-that-can-compensate-for-tilt-and-pitch

internal object AzimuthCalculator {

    fun calculate(gravity: FloatArray, magneticField: FloatArray): Bearing? {
        // East vector - perpendicular to gravity and magnetic field
        val east = Vector3Utils.cross(gravity, magneticField)
        val normEast = Vector3Utils.normalize(east)

        // North vector - perpendicular to gravity and east
        val north = Vector3Utils.cross(gravity, east)
        val normNorth = Vector3Utils.normalize(north)

        // Azimuth
        // Derived from the rotation matrix with X = Y
        val azimuth = atan2(normEast[0], normNorth[0]).toDegrees() + 90

        if (azimuth.isNaN()) {
            return null
        }

        return Bearing(azimuth)
    }


    fun calculate(gravity: Vector3, magneticField: Vector3): Bearing? {
        return calculate(gravity.toFloatArray(), magneticField.toFloatArray())
    }

}