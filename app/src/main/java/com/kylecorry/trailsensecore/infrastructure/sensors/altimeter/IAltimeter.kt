package com.kylecorry.trailsensecore.infrastructure.sensors.altimeter

import com.kylecorry.sense.ISensor

interface IAltimeter: ISensor {
    val altitude: Float
}