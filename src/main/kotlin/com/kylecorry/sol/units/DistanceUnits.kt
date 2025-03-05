package com.kylecorry.sol.units

enum class DistanceUnits(val id: Int, val meters: Float, val isMetric: Boolean) {
    Centimeters(1, 0.01f, true),
    Inches(2, 1 / (3.28084f * 12f), false),
    Miles(3, 5280f / 3.28084f, false),
    Yards(4, 0.9144f, false),
    Feet(5, 1 / 3.28084f, false),
    Kilometers(6, 1000f, true),
    Meters(7, 1f, true),
    NauticalMiles(8, 1852f, false),
    Millimeters(9, 0.001f, true),
}