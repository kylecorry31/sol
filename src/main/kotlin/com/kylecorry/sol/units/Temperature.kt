package com.kylecorry.sol.units

@JvmInline
value class Temperature private constructor(private val measure: Measure) : Comparable<Temperature> {
    val value: Float
        get() = measureValue(measure)

    val units: TemperatureUnits
        get() = measureUnit<TemperatureUnits>(measure)

    fun convertTo(toUnits: TemperatureUnits): Temperature {
        if (units == toUnits) {
            return this
        }

        val c = when (units) {
            TemperatureUnits.C -> value
            TemperatureUnits.F -> (value - 32) * 5 / 9
        }

        val newTemp = when (toUnits) {
            TemperatureUnits.C -> c
            TemperatureUnits.F -> (c * 9 / 5) + 32
        }

        return from(newTemp, toUnits)
    }

    fun celsius(): Temperature {
        return convertTo(TemperatureUnits.C)
    }

    override fun compareTo(other: Temperature): Int {
        return celsius().value.compareTo(other.celsius().value)
    }

    companion object {
        val zero = celsius(0f)

        fun from(value: Float, unit: TemperatureUnits): Temperature {
            return Temperature(packMeasure(value, unit))
        }

        fun celsius(temperature: Float): Temperature {
            return from(temperature, TemperatureUnits.C)
        }

        fun fahrenheit(temperature: Float): Temperature {
            return from(temperature, TemperatureUnits.F)
        }
    }
}