package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import kotlin.math.PI

internal object FastFourierTransform {

    fun fft(data: List<Float>, twiddleFactors: List<ComplexNumber> = getTwiddleFactors(data.size)): List<ComplexNumber> {
        val complexData = data.map { ComplexNumber(it, 0f) }
        return fftComplex(complexData, twiddleFactors)
    }

    private fun fftComplex(data: List<ComplexNumber>, twiddleFactors: List<ComplexNumber>, twiddleStep: Int = 1): List<ComplexNumber> {
        val size = data.size

        if (size <= 1) {
            return data
        }

        if (size and (size - 1) != 0) {
            throw IllegalArgumentException("Length of inputSequence must be a power of 2")
        }

        // Split input into even and odd indexed elements
        val evenSequence = fftComplex(data.filterIndexed { index, _ -> index % 2 == 0 }, twiddleFactors, twiddleStep * 2)
        val oddSequence = fftComplex(data.filterIndexed { index, _ -> index % 2 != 0 }, twiddleFactors, twiddleStep * 2)

        // Combine the results
        val combinedSequence = MutableList(size) { ComplexNumber(0f, 0f) }
        for (k in 0..<size / 2) {
            val twiddleOddK = twiddleFactors[twiddleStep * k] * oddSequence[k]
            combinedSequence[k] = evenSequence[k] + twiddleOddK
            combinedSequence[k + size / 2] = evenSequence[k] - twiddleOddK
        }

        return combinedSequence
    }

    fun ifft(fft: List<ComplexNumber>, twiddleFactors: List<ComplexNumber> = getTwiddleFactors(fft.size)): List<Float> {
        val size = fft.size
        val conjugatedInput = fft.map { it.conjugate() }
        val fftOfConjugated = fftComplex(conjugatedInput, twiddleFactors)
        val conjugatedResult = fftOfConjugated.map { it.conjugate() }
        return conjugatedResult.map { it.real / size }
    }

    fun getTwiddleFactors(size: Int): List<ComplexNumber> {
        return List(size / 2) { k ->
            val twiddleIndex = k.toFloat() / size.toFloat()
            ComplexNumber.exp(-2 * PI.toFloat() * twiddleIndex)
        }
    }

}
