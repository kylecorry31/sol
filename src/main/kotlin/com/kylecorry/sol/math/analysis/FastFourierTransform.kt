package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import kotlin.math.PI

internal object FastFourierTransform {

    fun fft(data: List<Float>): List<ComplexNumber> {
        val complexData = data.map { ComplexNumber(it, 0f) }
        return fftComplex(complexData)
    }

    private fun fftComplex(data: List<ComplexNumber>): List<ComplexNumber> {
        val size = data.size

        if (size <= 1) {
            return data
        }

        if (size and (size - 1) != 0) {
            throw IllegalArgumentException("Length of inputSequence must be a power of 2")
        }

        // Split input into even and odd indexed elements
        val evenSequence = fftComplex(data.filterIndexed { index, _ -> index % 2 == 0 })
        val oddSequence = fftComplex(data.filterIndexed { index, _ -> index % 2 != 0 })

        // Compute the twiddle factors
        val twiddleFactors =
            List(size / 2) { k -> ComplexNumber.exp(-2 * PI.toFloat() * k / size.toFloat()) }

        // Combine the results
        val combinedSequence = MutableList(size) { ComplexNumber(0f, 0f) }
        for (k in 0..<size / 2) {
            val twiddleOddK = twiddleFactors[k] * oddSequence[k]
            combinedSequence[k] = evenSequence[k] + twiddleOddK
            combinedSequence[k + size / 2] = evenSequence[k] - twiddleOddK
        }

        return combinedSequence
    }

    fun ifft(fft: List<ComplexNumber>): List<Float> {
        val size = fft.size
        val conjugatedInput = fft.map { it.conjugate() }
        val fftOfConjugated = fftComplex(conjugatedInput)
        val conjugatedResult = fftOfConjugated.map { it.conjugate() }
        return conjugatedResult.map { it.real / size }
    }

}
