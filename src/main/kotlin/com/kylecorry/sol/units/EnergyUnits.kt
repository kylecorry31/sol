package com.kylecorry.sol.units

enum class EnergyUnits(val id: Int, val joules: Float, val isMetric: Boolean) {
    Joules(1, 1f, true),
    FootPounds(2, 1.355818f, false),
}