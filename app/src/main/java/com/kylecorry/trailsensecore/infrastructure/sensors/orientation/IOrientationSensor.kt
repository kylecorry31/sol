package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import com.kylecorry.trailsensecore.domain.math.Quaternion
import com.kylecorry.sense.ISensor

interface IOrientationSensor: ISensor {
    val orientation: Quaternion
    val rawOrientation: FloatArray
}