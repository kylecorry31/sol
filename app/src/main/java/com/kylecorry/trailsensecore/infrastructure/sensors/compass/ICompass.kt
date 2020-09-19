package com.kylecorry.trailsensecore.infrastructure.sensors.compass

import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface ICompass: ISensor {
    val bearing: Bearing
    var declination: Float
}