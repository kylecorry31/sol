package com.kylecorry.trailsensecore.infrastructure.sensors.hygrometer

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IHygrometer: ISensor {
    val humidity: Float
}