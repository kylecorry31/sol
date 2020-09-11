package com.kylecorry.trailsensecore.infrastructure.sensors.gps

import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.infrastructure.sensors.altimeter.IAltimeter
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IGPS: ISensor, IAltimeter {
    val location: Coordinate
    val speed: Float
    val verticalAccuracy: Float?
    val horizontalAccuracy: Float?
    val satellites: Int
}