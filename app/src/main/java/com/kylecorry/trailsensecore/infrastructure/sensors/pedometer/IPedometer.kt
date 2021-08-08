package com.kylecorry.trailsensecore.infrastructure.sensors.pedometer

import com.kylecorry.sense.ISensor

interface IPedometer: ISensor {
    val steps: Int
}