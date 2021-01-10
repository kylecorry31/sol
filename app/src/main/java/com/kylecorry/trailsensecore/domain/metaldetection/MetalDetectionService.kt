package com.kylecorry.trailsensecore.domain.metaldetection

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
}