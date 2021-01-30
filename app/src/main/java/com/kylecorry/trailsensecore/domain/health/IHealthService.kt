package com.kylecorry.trailsensecore.domain.health

import com.kylecorry.trailsensecore.domain.health.heart.IHeartService
import com.kylecorry.trailsensecore.domain.health.temperature.ITemperatureService
import com.kylecorry.trailsensecore.domain.health.weight.BMI
import com.kylecorry.trailsensecore.domain.health.weight.Weight
import com.kylecorry.trailsensecore.domain.units.Distance
import java.time.Duration

interface IHealthService: IHeartService, ITemperatureService {
    /**
     * Gets the heart rate in beats per minute
     */
    fun getHeartRate(beats: Int, duration: Duration): Float

    fun calculateBMI(weight: Weight, height: Distance): BMI
}