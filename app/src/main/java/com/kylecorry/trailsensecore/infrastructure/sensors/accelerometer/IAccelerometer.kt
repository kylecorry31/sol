package com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.sense.ISensor

interface IAccelerometer: ISensor {
    val acceleration: Vector3
    val rawAcceleration: FloatArray
}