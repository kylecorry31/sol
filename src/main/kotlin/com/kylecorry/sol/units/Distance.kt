package com.kylecorry.sol.units

enum class Distance(override val id: Int, override val multiplierToBase: Float, val isMetric: Boolean) : PhysicalUnit {
    Centimeters(1, 0.01f, true),
    Inches(2, 1 / (3.28084f * 12), false),
    Miles(3, 5280 / 3.28084f, false),
    Yards(4, 0.9144f, false),
    Feet(5, 1 / 3.28084f, false),
    Kilometers(6, 1000f, true),
    Meters(7, 1f, true),
    NauticalMiles(8, 1852f, false),
    Millimeters(9, 0.001f, true);

    companion object {
        fun meters(amount: Float): Quantity<Distance> {
            return Quantity(amount, Meters)
        }

        fun kilometers(amount: Float): Quantity<Distance> {
            return Quantity(amount, Kilometers)
        }

        fun miles(amount: Float): Quantity<Distance> {
            return Quantity(amount, Miles)
        }

        fun feet(amount: Float): Quantity<Distance> {
            return Quantity(amount, Feet)
        }

        fun inches(amount: Float): Quantity<Distance> {
            return Quantity(amount, Inches)
        }

        fun centimeters(amount: Float): Quantity<Distance> {
            return Quantity(amount, Centimeters)
        }

        fun yards(amount: Float): Quantity<Distance> {
            return Quantity(amount, Yards)
        }

        fun nauticalMiles(amount: Float): Quantity<Distance> {
            return Quantity(amount, NauticalMiles)
        }

        fun millimeters(amount: Float): Quantity<Distance> {
            return Quantity(amount, Millimeters)
        }
    }
}

fun Quantity<Distance>.meters(): Quantity<Distance> {
    return convertTo(Distance.Meters)
}