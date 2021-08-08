package com.kylecorry.trailsensecore.infrastructure.sensors.network

import com.kylecorry.trailsensecore.domain.network.CellSignal
import com.kylecorry.sense.ISensor

interface ICellSignalSensor: ISensor {
    val signals: List<CellSignal>
}