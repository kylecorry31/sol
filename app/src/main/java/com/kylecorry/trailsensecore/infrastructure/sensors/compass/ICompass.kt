package com.kylecorry.trailsensecore.infrastructure.sensors.compass

import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.sense.ISensor

interface ICompass: ISensor {
    val bearing: Bearing
    val rawBearing: Float
    var declination: Float
}