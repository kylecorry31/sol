package com.kylecorry.trailsensecore.domain.units

data class Temperature(val temperature: Float, val units: TemperatureUnits) {

    private val unitService = UnitService()

    fun convertTo(toUnits: TemperatureUnits): Temperature {
        return Temperature(unitService.convert(temperature, units, toUnits), toUnits)
    }
}