package com.kylecorry.trailsensecore.infrastructure.sensors.odometer

import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.sense.ISensor

interface IOdometer: ISensor {
    val distance: Distance
}