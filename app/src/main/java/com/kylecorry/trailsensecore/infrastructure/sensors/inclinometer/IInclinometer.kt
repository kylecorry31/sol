package com.kylecorry.trailsensecore.infrastructure.sensors.inclinometer

import com.kylecorry.sense.ISensor

interface IInclinometer: ISensor {
    val angle: Float
}