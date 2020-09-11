package com.kylecorry.trailsensecore.infrastructure.sensors.declination

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IDeclinationProvider: ISensor {

    val declination: Float

}