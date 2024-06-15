package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import com.kylecorry.sol.shared.ArrayUtils.swap
import kotlin.math.PI

internal object FastFourierTransform {

    fun fft(
        data: List<Float>,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactors(data.size)
    ): List<ComplexNumber> {
        val complexData = Array(data.size) { ComplexNumber(data[it], 0f) }
        bitReverse(complexData)
        return fftIterative(complexData, twiddleFactors).toList()
    }

    private fun fftIterative(data: Array<ComplexNumber>, twiddleFactors: List<ComplexNumber>): Array<ComplexNumber> {
        val n = data.size
        var m = 1
        while (m < n) {
            val m2 = m * 2
            for (k in 0..<m) {
                val twiddleFactor = twiddleFactors[k * n / m2]
                for (i in k..<n step m2) {
                    val t = twiddleFactor * data[i + m]
                    data[i + m] = data[i] - t
                    data[i] += t
                }
            }
            m = m2
        }
        return data
    }

    private fun bitReverse(data: Array<ComplexNumber>) {
        val n = data.size
        var j = 0
        for (i in 0..<n - 1) {
            if (i < j) {
                data.swap(i, j)
            }
            var k = n shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }
    }

    fun ifft(fft: List<ComplexNumber>, twiddleFactors: List<ComplexNumber> = getTwiddleFactors(fft.size)): List<Float> {
        val size = fft.size
        val conjugatedInput = Array(size) { fft[it].conjugate() }
        bitReverse(conjugatedInput)
        return fftIterative(conjugatedInput, twiddleFactors)
            .map { it.conjugate().real / size }
    }

    fun getTwiddleFactors(size: Int): List<ComplexNumber> {
        return List(size / 2) { k ->
            val twiddleIndex = k.toFloat() / size.toFloat()
            ComplexNumber.exp(-2 * PI.toFloat() * twiddleIndex)
        }
    }

}
