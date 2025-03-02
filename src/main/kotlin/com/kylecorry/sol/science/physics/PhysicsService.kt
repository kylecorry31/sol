package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.science.geology.Geology
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity
import java.time.Duration

class PhysicsService : IPhysicsService {

    override fun fallHeight(time: Duration, gravity: Float): Quantity<Distance> {
        val seconds = time.toMillis() / 1000f
        return Distance.meters(0.5f * gravity * seconds * seconds)
    }

    override fun isMetal(magneticField: Vector3, threshold: Float): Boolean {
        return isMetal(magneticField.magnitude(), threshold)
    }

    override fun isMetal(fieldStrength: Float, threshold: Float): Boolean {
        return fieldStrength >= threshold
    }

    override fun removeGeomagneticField(
        rawMagneticField: Vector3,
        geomagneticField: Vector3,
        orientationChange: Quaternion?
    ): Vector3 {
        val updatedGeo = orientationChange?.rotate(geomagneticField)
            ?: (rawMagneticField.normalize() * geomagneticField.magnitude())
        return rawMagneticField - updatedGeo
    }

    override fun getMetalDirection(
        magneticField: Vector3,
        gravity: Vector3
    ): Pair<Bearing, Bearing> {
        val azimuth = Geology.getAzimuth(gravity, magneticField)
        return azimuth to azimuth.inverse()
    }
}