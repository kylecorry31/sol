package com.kylecorry.sol.math.trigonometry

import kotlin.math.cos

class CosineWave(
    override val amplitude: Float,
    override val frequency: Float,
    override val horizontalShift: Float,
    override val verticalShift: Float,
) : Waveform {
    override fun calculate(x: Float): Float = amplitude * cos(frequency * (x - horizontalShift)) + verticalShift
}
