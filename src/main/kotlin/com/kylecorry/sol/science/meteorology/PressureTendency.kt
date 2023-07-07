package com.kylecorry.sol.science.meteorology

data class PressureTendency(val characteristic: PressureCharacteristic, val amount: Float){
    companion object {
        val zero = PressureTendency(PressureCharacteristic.Steady, 0f)
    }
}