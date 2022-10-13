package com.kylecorry.sol.science.meteorology

enum class PressureCharacteristic(val isFalling: Boolean, val isRising: Boolean) {
    Falling(true, false),
    FallingFast(true, false),
    Rising(false, true),
    RisingFast(false, true),
    Steady(false, false)
}