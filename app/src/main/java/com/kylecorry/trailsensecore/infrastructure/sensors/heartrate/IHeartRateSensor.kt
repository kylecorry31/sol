package com.kylecorry.trailsensecore.infrastructure.sensors.heartrate

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor
import java.time.Instant

interface IHeartRateSensor: ISensor {
    val pulseWave: List<Pair<Instant, Float>>
    val heartBeats: List<Instant>
    val bpm: Int
}