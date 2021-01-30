package com.kylecorry.trailsensecore.domain.health.temperature

import com.kylecorry.trailsensecore.domain.units.Temperature
import com.kylecorry.trailsensecore.domain.units.TemperatureUnits

class TemperatureService : ITemperatureService {

    override fun classifyBodyTemperature(temperature: Temperature): BodyTemperature {
        val f = temperature.convertTo(TemperatureUnits.F)
        return when {
            f.temperature < 95 -> {
                BodyTemperature.Hypothermia
            }
            f.temperature >= 100.4 -> {
                BodyTemperature.Hyperthermia
            }
            else -> {
                BodyTemperature.Normal
            }
        }
    }

}