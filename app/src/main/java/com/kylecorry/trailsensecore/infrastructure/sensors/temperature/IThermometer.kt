package com.kylecorry.trailsensecore.infrastructure.sensors.temperature

import com.kylecorry.sense.ISensor

interface IThermometer: ISensor {
    val temperature: Float
}