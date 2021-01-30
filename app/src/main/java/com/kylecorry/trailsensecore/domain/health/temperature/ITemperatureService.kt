package com.kylecorry.trailsensecore.domain.health.temperature

import com.kylecorry.trailsensecore.domain.units.Temperature

interface ITemperatureService {
    fun classifyBodyTemperature(temperature: Temperature): BodyTemperature
}