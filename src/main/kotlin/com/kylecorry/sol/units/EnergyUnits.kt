package com.kylecorry.sol.units

enum class EnergyUnits(val id: Int, val joules: Double, val isMetric: Boolean) {
    Joules(1, 1.0, true),
    FootPounds(2, 1.355818, false),
}