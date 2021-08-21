package com.kylecorry.trailsensecore.domain.health

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.health.heart.IHeartService
import com.kylecorry.trailsensecore.domain.health.temperature.ITemperatureService
import com.kylecorry.trailsensecore.domain.health.weight.BMI
import com.kylecorry.andromeda.core.units.Weight
import java.time.Duration

interface IHealthService: IHeartService, ITemperatureService {
    /**
     * Gets the heart rate in beats per minute
     */
    fun getHeartRate(beats: Int, duration: Duration): Float

    fun calculateBMI(weight: Weight, height: Distance): BMI
}