package com.kylecorry.sol.math.trigonometry

import kotlin.math.sin

class SineWave(
    override val amplitude: Float,
    override val frequency: Float,
    override val horizontalShift: Float,
    override val verticalShift: Float
) : Waveform {

    override fun calculate(x: Float): Float {
        return amplitude * sin(frequency * (x - horizontalShift)) + verticalShift
    }

}