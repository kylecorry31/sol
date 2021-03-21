package com.kylecorry.trailsensecore.infrastructure.sensors.odometer

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IOdometer: ISensor {
    val distance: Distance
}