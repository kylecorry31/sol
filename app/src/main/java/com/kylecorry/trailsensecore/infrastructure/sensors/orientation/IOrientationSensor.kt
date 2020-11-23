package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IOrientationSensor: ISensor {

    val orientation: Vector3

}