package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.math.Vector3Utils
import com.kylecorry.sol.units.Bearing
import kotlin.math.atan2

internal object AzimuthCalculator {

    fun calculate(acceleration: FloatArray, magneticField: FloatArray): Bearing? {
        // Down vector (from gravity)
        val down = Vector3Utils.normalize(Vector3Utils.times(acceleration, -1f), true)

        // East vector - perpendicular to down and magnetic field
        val east = Vector3Utils.normalize(Vector3Utils.cross(down, magneticField), true)

        // North vector - perpendicular to down and east
        val north = Vector3Utils.normalize(Vector3Utils.cross(east, down), true)

        // Azimuth
        // https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/hardware/SensorManager.java;l=91;bpv=1;bpt=0?q=SensorManager&sq=&ss=android%2Fplatform%2Fsuperproject
        // Derived from the rotation matrix with X = Y
        // This is the projection of the east and north vectors onto the X-Y plane (just their X component)
        val azimuth = atan2(-north[0], east[0]).toDegrees()

        if (azimuth.isNaN()) {
            return null
        }

        return Bearing(azimuth)
    }


    fun calculate(gravity: Vector3, magneticField: Vector3): Bearing? {
        return calculate(gravity.toFloatArray(), magneticField.toFloatArray())
    }

}