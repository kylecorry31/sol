package com.kylecorry.sol.math.analysis

interface Waveform {
    val amplitude: Float
    val frequency: Float
    val horizontalShift: Float
    val verticalShift: Float

    fun calculate(x: Float): Float
}
