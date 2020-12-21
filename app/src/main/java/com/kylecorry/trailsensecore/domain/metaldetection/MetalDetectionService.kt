package com.kylecorry.trailsensecore.domain.metaldetection

import com.kylecorry.trailsensecore.domain.math.Vector3

class MetalDetectionService : IMetalDetectionService {
    override fun isMetal(magneticField: Vector3, threshold: Float): Boolean {
        val strength = getFieldStrength(magneticField)
        return strength >= threshold
    }

    override fun getFieldStrength(magneticField: Vector3): Float {
        return magneticField.magnitude()
    }
}