package com.kylecorry.sol.units

enum class AngleUnits(val id: Int, val radians: Float) {
    Degrees(1, 0.0174533f),
    Radians(2, 1f),
    Mils(3, 0.0001570795f)
}