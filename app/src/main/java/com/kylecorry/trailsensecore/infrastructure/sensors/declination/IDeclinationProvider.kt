package com.kylecorry.trailsensecore.infrastructure.sensors.declination

import com.kylecorry.sense.ISensor

interface IDeclinationProvider: ISensor {

    val declination: Float

}