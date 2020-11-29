package com.kylecorry.trailsensecore.infrastructure.sensors.gps

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.infrastructure.sensors.altimeter.IAltimeter
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor
import com.kylecorry.trailsensecore.infrastructure.sensors.clock.IClock

interface IGPS: ISensor, IAltimeter, IClock {
    val location: Coordinate
    val speed: Float
    val verticalAccuracy: Float?
    val horizontalAccuracy: Float?
    val satellites: Int
}