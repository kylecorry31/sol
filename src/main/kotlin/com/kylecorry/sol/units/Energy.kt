package com.kylecorry.sol.units

data class Energy(val value: Float, val units: EnergyUnits) {

    fun convertTo(newUnits: EnergyUnits): Energy {
        if (units == newUnits) {
            return this
        }
        val joules = value * units.joules
        return Energy(joules / newUnits.joules, newUnits)
    }

    fun joules(): Energy {
        return convertTo(EnergyUnits.Joules)
    }
}