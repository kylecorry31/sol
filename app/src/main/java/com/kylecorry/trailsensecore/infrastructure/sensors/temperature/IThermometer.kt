package com.kylecorry.trailsensecore.infrastructure.sensors.temperature

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IThermometer: ISensor {
    val temperature: Float
}