package com.kylecorry.trailsensecore.infrastructure.sensors.clock

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor
import java.time.Instant

interface IClock: ISensor {
    val time: Instant
}