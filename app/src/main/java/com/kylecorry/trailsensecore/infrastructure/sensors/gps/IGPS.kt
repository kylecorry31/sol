package com.kylecorry.trailsensecore.infrastructure.sensors.gps

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.infrastructure.sensors.altimeter.IAltimeter
import com.kylecorry.sense.ISensor
import com.kylecorry.trailsensecore.infrastructure.sensors.clock.IClock
import com.kylecorry.trailsensecore.infrastructure.sensors.speedometer.ISpeedometer

interface IGPS: ISensor, IAltimeter, IClock, ISpeedometer {
    val location: Coordinate
    val verticalAccuracy: Float?
    val horizontalAccuracy: Float?
    val satellites: Int
    val mslAltitude: Float?
}