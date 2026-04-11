package com.kylecorry.sol.units

enum class DistanceUnits(val id: Int, val meters: Double, val isMetric: Boolean) {
    Centimeters(1, 0.01, true),
    Inches(2, 1 / (3.28084 * 12), false),
    Miles(3, 5280 / 3.28084, false),
    Yards(4, 0.9144, false),
    Feet(5, 1 / 3.28084, false),
    Kilometers(6, 1000.0, true),
    Meters(7, 1.0, true),
    NauticalMiles(8, 1852.0, false),
    Millimeters(9, 0.001, true),
    Caliber(10, 0.0254, false),
}