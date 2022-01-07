package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import com.kylecorry.sol.math.Range
import kotlin.math.cos
import kotlin.math.sin

class DiscreteFourierTransform {

    fun single(x: List<Float>, y: List<Float>, frequency: Float): ComplexNumber {
        var real = 0f
        var imaginary = 0f
        val xRange = x.last() - x.first()
        val dx = xRange / x.size
        for (i in x.indices) {
            real += y[i] * cos(frequency * x[i]) * dx
            imaginary -= y[i] * sin(frequency * x[i]) * dx
        }
        real *= 2 / xRange
        imaginary *= 2 / xRange

        return ComplexNumber(real, imaginary)
    }

    fun range(
        x: List<Float>,
        y: List<Float>,
        frequencies: Range<Float>,
        interval: Float
    ): List<ComplexNumber> {
        val complexNumbers = mutableListOf<ComplexNumber>()

        var frequency = frequencies.start
        while (frequency <= frequencies.end) {
            complexNumbers.add(single(x, y, frequency))
            frequency += interval
        }

        return complexNumbers
    }

}