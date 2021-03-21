package com.kylecorry.trailsensecore.infrastructure.sensors.speedometer

import com.kylecorry.trailsensecore.domain.units.Speed
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface ISpeedometer: ISensor {
    val speed: Speed
}