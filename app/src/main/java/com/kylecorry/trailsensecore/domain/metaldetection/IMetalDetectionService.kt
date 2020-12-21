package com.kylecorry.trailsensecore.domain.metaldetection

import com.kylecorry.trailsensecore.domain.math.Vector3

interface IMetalDetectionService {
    fun isMetal(magneticField: Vector3, threshold: Float = 65f): Boolean
    fun getFieldStrength(magneticField: Vector3): Float
}