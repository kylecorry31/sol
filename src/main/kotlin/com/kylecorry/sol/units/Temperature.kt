package com.kylecorry.sol.units

enum class Temperature(override val id: Int, override val multiplierToBase: Float, override val offsetToBase: Float) :
    PhysicalUnit {
    Fahrenheit(1, 5 / 9f, -32f),
    Celsius(2, 1f, 0f);

    companion object {
        val zero = celsius(0f)

        fun celsius(temperature: Float): Quantity<Temperature> {
            return Quantity(temperature, Celsius)
        }

        fun fahrenheit(temperature: Float): Quantity<Temperature> {
            return Quantity(temperature, Fahrenheit)
        }
    }
}

fun Quantity<Temperature>.celsius(): Quantity<Temperature> {
    return convertTo(Temperature.Celsius)
}