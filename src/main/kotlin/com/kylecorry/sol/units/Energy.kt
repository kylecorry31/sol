package com.kylecorry.sol.units

@JvmInline
value class Energy private constructor(private val measure: Measure) {
    val value: Float
        get() = measureValue(measure)

    val units: EnergyUnits
        get() = measureUnit<EnergyUnits>(measure)

    fun convertTo(newUnits: EnergyUnits): Energy {
        if (units == newUnits) {
            return this
        }
        val joules = value * units.joules
        return from((joules / newUnits.joules).toFloat(), newUnits)
    }

    override fun toString(): String {
        return "$value $units"
    }

    fun joules(): Energy {
        return convertTo(EnergyUnits.Joules)
    }

    companion object {
        fun from(value: Float, units: EnergyUnits): Energy {
            return Energy(packMeasure(value, units))
        }
    }
}