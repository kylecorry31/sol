package com.kylecorry.trailsensecore.infrastructure.sensors.magnetometer

import com.kylecorry.trailsensecore.domain.math.Vector3
import com.kylecorry.sense.ISensor

interface IMagnetometer: ISensor {
    val magneticField: Vector3
    val rawMagneticField: FloatArray
}