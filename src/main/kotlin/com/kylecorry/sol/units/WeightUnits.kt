package com.kylecorry.sol.units

enum class WeightUnits(val id: Int, val grams: Float) {
    Pounds(1, 453.592f),
    Ounces(2, 28.3495f),
    Kilograms(3, 1000f),
    Grams(4, 1f),
    Milligrams(5, 0.001f),
    Grains(6, 0.06479891f),
}