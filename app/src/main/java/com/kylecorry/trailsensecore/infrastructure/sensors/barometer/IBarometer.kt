package com.kylecorry.trailsensecore.infrastructure.sensors.barometer

import com.kylecorry.trailsensecore.infrastructure.sensors.altimeter.IAltimeter
import com.kylecorry.sense.ISensor

interface IBarometer: ISensor, IAltimeter {
    val pressure: Float
}