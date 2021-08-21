package com.kylecorry.trailsensecore.domain.health.temperature

import com.kylecorry.andromeda.core.units.Temperature

interface ITemperatureService {
    fun classifyBodyTemperature(temperature: Temperature): BodyTemperature
}