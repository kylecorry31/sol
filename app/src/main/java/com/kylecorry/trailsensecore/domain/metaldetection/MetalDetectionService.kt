package com.kylecorry.trailsensecore.domain.metaldetection

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.trailsensecore.domain.geo.AzimuthCalculator
import com.kylecorry.trailsensecore.domain.math.Quaternion
import com.kylecorry.trailsensecore.domain.math.Vector3

class MetalDetectionService : IMetalDetectionService {

    override fun isMetal(magneticField: Vector3, threshold: Float): Boolean {
        val strength = getFieldStrength(magneticField)
        return isMetal(strength, threshold)
    }

    override fun isMetal(fieldStrength: Float, threshold: Float): Boolean {
        return fieldStrength >= threshold
    }

    override fun getFieldStrength(magneticField: Vector3): Float {
        return magneticField.magnitude()
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
        val azimuth =
            AzimuthCalculator.calculate(gravity, magneticField, includeMagnitudeCheck = false)
                ?: Bearing(0f)
        return azimuth to azimuth.inverse()
    }
}