package com.kylecorry.sol.units

enum class PressureUnits(val id: Int, val hpa: Double) {
    Hpa(1, 1.0),
    Mbar(2, 1.0),
    Inhg(3, 1 / 0.02953),
    Psi(4, 1 / 0.014503774),
    MmHg(5, 1.3332239)
}