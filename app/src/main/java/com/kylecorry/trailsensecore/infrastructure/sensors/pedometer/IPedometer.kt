package com.kylecorry.trailsensecore.infrastructure.sensors.pedometer

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IPedometer: ISensor {
    val steps: Int
}