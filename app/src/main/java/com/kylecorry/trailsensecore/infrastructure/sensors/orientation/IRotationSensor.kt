package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import com.kylecorry.trailsensecore.domain.math.Euler
import com.kylecorry.trailsensecore.domain.math.Quaternion
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IRotationSensor: ISensor {
    val rawEuler: FloatArray
    val euler: Euler
    val quaternion: Quaternion
    val rawQuaternion: FloatArray
}