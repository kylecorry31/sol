package com.kylecorry.trailsensecore.infrastructure.sensors.light

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface ILightSensor: ISensor {
    val illuminance: Float
}