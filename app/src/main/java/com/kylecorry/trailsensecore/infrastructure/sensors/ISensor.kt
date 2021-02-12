package com.kylecorry.trailsensecore.infrastructure.sensors

import com.kylecorry.trailsensecore.domain.units.Quality

interface ISensor {

    val quality: Quality

    val hasValidReading: Boolean

    fun start(listener: SensorListener)

    fun stop(listener: SensorListener?)

}