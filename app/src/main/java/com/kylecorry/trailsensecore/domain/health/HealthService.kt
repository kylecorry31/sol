package com.kylecorry.trailsensecore.domain.health

import com.kylecorry.trailsensecore.domain.health.heart.BloodPressure
import com.kylecorry.trailsensecore.domain.health.heart.BloodPressureCategory
import com.kylecorry.trailsensecore.domain.health.heart.HeartService
import com.kylecorry.trailsensecore.domain.health.heart.PulseOxygenCategory
import com.kylecorry.trailsensecore.domain.health.temperature.BodyTemperature
import com.kylecorry.trailsensecore.domain.health.temperature.TemperatureService
import com.kylecorry.trailsensecore.domain.units.Temperature
import java.time.Duration

class HealthService : IHealthService {

    private val heartService = HeartService()
    private val temperatureService = TemperatureService()

    override fun getHeartRate(beats: Int, duration: Duration): Float {
        if (duration.isZero) {
            return 0f
        }

        return beats / (duration.toMillis() / 1000f / 60f)
    }

    override fun classifyBloodPressure(pressure: BloodPressure): BloodPressureCategory {
        return heartService.classifyBloodPressure(pressure)
    }

    override fun classifyPulseOxygen(percent: Float): PulseOxygenCategory {
        return heartService.classifyPulseOxygen(percent)
    }

    override fun classifyBodyTemperature(temperature: Temperature): BodyTemperature {
        return temperatureService.classifyBodyTemperature(temperature)
    }

}