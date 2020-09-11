package com.kylecorry.trailsensecore.infrastructure.sensors.accelerometer

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IAccelerometer: ISensor {
    val acceleration: Vector3
}