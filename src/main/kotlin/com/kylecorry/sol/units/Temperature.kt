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
            TemperatureUnits.Celsius -> value.toDouble()
            TemperatureUnits.Fahrenheit -> (value - 32) * 5 / 9.0
        }

        val newTemp = when (toUnits) {
            TemperatureUnits.Celsius -> c
            TemperatureUnits.Fahrenheit -> (c * 9 / 5.0) + 32
        }

        return from(newTemp.toFloat(), toUnits)
    }

    fun celsius(): Temperature {
        return convertTo(TemperatureUnits.Celsius)
    }

    override fun toString(): String {
        return "$value $units"
    }

    override fun compareTo(other: Temperature): Int {
        return celsius().value.compareTo(other.celsius().value)
    }

    companion object {
        val zero = celsius(0f)

        val ABSOLUTE_ZERO = celsius(-273.15f)

        fun from(value: Float, unit: TemperatureUnits): Temperature {
            return Temperature(packMeasure(value, unit))
        }

        fun celsius(temperature: Float): Temperature {
            return from(temperature, TemperatureUnits.Celsius)
        }

        fun fahrenheit(temperature: Float): Temperature {
            return from(temperature, TemperatureUnits.Fahrenheit)
        }
    }
}