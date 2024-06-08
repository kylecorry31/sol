package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber

object FrequencyAnalysis {

    // Analysis

    fun fft(data: List<Float>): List<ComplexNumber> {
        return FastFourierTransform.fft(data)
    }

    fun getMostResonantFrequency(data: List<Float>, sampleRate: Float): Float? {
        return getMostResonantFrequencies(data, sampleRate, 1).firstOrNull()
    }

    fun getMostResonantFrequencies(data: List<Float>, sampleRate: Float, count: Int): List<Float> {
        return getMostResonantFrequenciesFFT(fft(data), sampleRate, count)
    }

    fun getFrequencySpectrum(data: List<Float>, sampleRate: Float): List<Pair<Float, Float>> {
        return getFrequencySpectrumFFT(fft(data), sampleRate)
    }

    fun getMagnitudeOfFrequency(data: List<Float>, frequency: Float, sampleRate: Float): Float {
        return getMagnitudeOfFrequencyFFT(fft(data), frequency, sampleRate)
    }

    // OPERATIONS ON FFT

    fun getMostResonantFrequencyFFT(fft: List<ComplexNumber>, sampleRate: Float): Float? {
        return getMostResonantFrequenciesFFT(fft, sampleRate, 1).firstOrNull()
    }

    fun getMostResonantFrequenciesFFT(fft: List<ComplexNumber>, sampleRate: Float, count: Int): List<Float> {
        return fft
            .withIndex()
            .sortedByDescending { it.value.magnitude }
            .take(count)
            .map { getFrequencyFFT(it.index, fft.size, sampleRate) }
    }

    fun getFrequencySpectrumFFT(fft: List<ComplexNumber>, sampleRate: Float): List<Pair<Float, Float>> {
        return fft
            .withIndex()
            .map { Pair(getFrequencyFFT(it.index, fft.size, sampleRate), it.value.magnitude) }
    }

    fun getMagnitudeOfFrequencyFFT(fft: List<ComplexNumber>, frequency: Float, sampleRate: Float): Float {
        val index = getIndexFFT(frequency, fft.size, sampleRate)
        return fft[index].magnitude
    }

    fun getFrequencyFFT(index: Int, size: Int, sampleRate: Float): Float {
        return index.toFloat() / size * sampleRate
    }

    fun getIndexFFT(frequency: Float, size: Int, sampleRate: Float): Int {
        return (frequency / sampleRate * size).toInt()
    }

}