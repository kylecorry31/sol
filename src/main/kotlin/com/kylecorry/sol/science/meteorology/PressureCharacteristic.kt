package com.kylecorry.sol.science.meteorology

enum class PressureCharacteristic(val isFalling: Boolean, val isRising: Boolean, val isRapid: Boolean) {
    Falling(true, false, false),
    FallingFast(true, false, true),
    Rising(false, true, false),
    RisingFast(false, true, true),
    Steady(false, false, false)
}