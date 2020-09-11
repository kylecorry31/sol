package com.kylecorry.trailsensecore.infrastructure.sensors.inclinometer

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IInclinometer: ISensor {
    val angle: Float
}