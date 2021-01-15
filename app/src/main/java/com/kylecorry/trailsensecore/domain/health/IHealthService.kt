package com.kylecorry.trailsensecore.domain.health

import java.time.Duration

interface IHealthService {
    /**
     * Gets the heart rate in beats per minute
     */
    fun getHeartRate(beats: Int, duration: Duration): Float
}