package com.kylecorry.trailsensecore.infrastructure.sensors.altimeter

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IAltimeter: ISensor {
    val altitude: Float
}