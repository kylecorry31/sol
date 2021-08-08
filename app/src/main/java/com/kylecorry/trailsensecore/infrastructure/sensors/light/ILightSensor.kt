package com.kylecorry.trailsensecore.infrastructure.sensors.light

import com.kylecorry.sense.ISensor

interface ILightSensor: ISensor {
    val illuminance: Float
}