package com.kylecorry.sol.units

enum class WeightUnits(val id: Int, val grams: Double) {
    Pounds(1, 453.592),
    Ounces(2, 28.3495),
    Kilograms(3, 1000.0),
    Grams(4, 1.0),
    Milligrams(5, 0.001),
    Grains(6, 0.06479891),
}