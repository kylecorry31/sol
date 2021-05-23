package com.kylecorry.trailsensecore.infrastructure.sensors.orientation

import com.kylecorry.trailsensecore.domain.math.Euler

interface IGyroscope: IOrientationSensor {
    val angularRate: Euler
    val rawAngularRate: FloatArray
}