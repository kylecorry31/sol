package com.kylecorry.trailsensecore.infrastructure.sensors

import com.kylecorry.trailsensecore.domain.Accuracy

interface ISensor {

    val accuracy: Accuracy

    val hasValidReading: Boolean

    fun start(listener: SensorListener)

    fun stop(listener: SensorListener?)

}