package com.kylecorry.trailsensecore.infrastructure.sensors.magnetometer

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IMagnetometer: ISensor {
    val magneticField: Vector3
}