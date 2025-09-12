package com.kylecorry.sol.units

@JvmInline
value class Pressure private constructor(private val measure: Measure) : Comparable<Pressure> {
    val pressure: Float
        get() = measureValue(measure)

    val units: PressureUnits
        get() = measureUnit<PressureUnits>(measure)

    fun convertTo(toUnits: PressureUnits): Pressure {
        if (units == toUnits) {
            return this
        }
        val hpa = pressure * units.hpa
        val newPressure = hpa / toUnits.hpa
        return from(newPressure, toUnits)
    }

    fun hpa(): Pressure {
        return convertTo(PressureUnits.Hpa)
    }

    override fun compareTo(other: Pressure): Int {
        val hpa = hpa().pressure
        val otherHpa = other.hpa().pressure
        return hpa.compareTo(otherHpa)
    }

    companion object {
        fun hpa(pressure: Float): Pressure {
            return from(pressure, PressureUnits.Hpa)
        }

        fun from(value: Float, unit: PressureUnits): Pressure {
            return Pressure(packMeasure(value, unit))
        }
    }
}