package com.kylecorry.trailsensecore.infrastructure.sensors.network

import com.kylecorry.trailsensecore.domain.network.CellSignal
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface ICellSignalSensor: ISensor {
    val signals: List<CellSignal>
}