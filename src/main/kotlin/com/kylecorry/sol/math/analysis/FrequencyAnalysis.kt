package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import kotlin.math.PI
import kotlin.math.cos

object FrequencyAnalysis {

    // Analysis

    fun fft(
        data: List<Float>,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactorsFFT(data.size)
    ): List<ComplexNumber> {
        return FastFourierTransform.fft(data, twiddleFactors)
    }

    fun ifft(
        fft: List<ComplexNumber>,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactorsFFT(fft.size)
    ): List<Float> {
        return FastFourierTransform.ifft(fft, twiddleFactors)
    }

    fun getMostResonantFrequency(
        data: List<Float>,
        sampleRate: Float,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactorsFFT(data.size)
    ): Float? {
        return getMostResonantFrequencies(data, sampleRate, 1, twiddleFactors).firstOrNull()
    }

    fun getMostResonantFrequencies(
        data: List<Float>,
        sampleRate: Float,
        count: Int,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactorsFFT(data.size)
    ): List<Float> {
        return getMostResonantFrequenciesFFT(fft(data, twiddleFactors), sampleRate, count)
    }

    fun getFrequencySpectrum(
        data: List<Float>,
        sampleRate: Float,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactorsFFT(data.size)
    ): List<Pair<Float, Float>> {
        return getFrequencySpectrumFFT(fft(data, twiddleFactors), sampleRate)
    }

    fun getMagnitudeOfFrequency(
        data: List<Float>,
        frequency: Float,
        sampleRate: Float,
        twiddleFactors: List<ComplexNumber> = getTwiddleFactorsFFT(data.size)
    ): Float {
        return getMagnitudeOfFrequencyFFT(fft(data, twiddleFactors), frequency, sampleRate)
    }

    fun hanningWindow(length: Int): FloatArray {
        val window = FloatArray(length)
        for (i in 0..<length) {
            window[i] = 0.5f * (1 - cos(2 * PI.toFloat() * i / (length - 1).toFloat()))
        }
        return window
    }

    fun hanningWindow(data: List<Float>): List<Float> {
        val window = hanningWindow(data.size)
        return data.mapIndexed { index, value -> value * window[index] }
    }

    // OPERATIONS ON FFT

    fun getMostResonantFrequencyFFT(fft: List<ComplexNumber>, sampleRate: Float): Float? {
        return getMostResonantFrequenciesFFT(fft, sampleRate, 1).firstOrNull()
    }

    fun getMostResonantFrequenciesFFT(fft: List<ComplexNumber>, sampleRate: Float, count: Int): List<Float> {
        return fft
            .asSequence()
            .take(fft.size / 2)
            .withIndex()
            .sortedByDescending { it.value.magnitude }
            .take(count)
            .map { getFrequencyFFT(it.index, fft.size, sampleRate) }
            .toList()
    }

    fun getFrequencySpectrumFFT(fft: List<ComplexNumber>, sampleRate: Float): List<Pair<Float, Float>> {
        return fft
            .mapIndexed { index, value -> Pair(getFrequencyFFT(index, fft.size, sampleRate), value.magnitude) }
    }

    fun getMagnitudeOfFrequencyFFT(fft: List<ComplexNumber>, frequency: Float, sampleRate: Float): Float {
        val index = getIndexFFT(frequency, fft.size, sampleRate)
        return fft[index].magnitude
    }

    fun getFrequencyFFT(index: Int, size: Int, sampleRate: Float): Float {
        val value = sampleRate / size
        val N = (size - 1) / 2 + 1
        return if (index < N) {
            index * value
        } else {
            (index - size) * value
        }
    }

    fun getIndexFFT(frequency: Float, size: Int, sampleRate: Float): Int {
        val value = sampleRate / size
        return if (frequency >= 0) {
            (frequency / value).toInt()
        } else {
            (size + frequency / value).toInt()
        }
    }

    /**
     * Get the twiddle factors for the FFT (used for the FFT and IFFT - precompute these for performance)
     * @param size The size of the FFT
     * @return The twiddle factors
     */
    fun getTwiddleFactorsFFT(size: Int): List<ComplexNumber> {
        return FastFourierTransform.getTwiddleFactors(size)
    }

}