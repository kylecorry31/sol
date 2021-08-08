package com.kylecorry.trailsensecore.infrastructure.sensors.hygrometer

import com.kylecorry.sense.ISensor

interface IHygrometer: ISensor {
    val humidity: Float
}