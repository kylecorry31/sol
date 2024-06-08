package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import kotlin.math.PI
import kotlin.math.cos

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

    fun spectogram(data: List<Float>, windowSize: Int, overlap: Int): List<List<Float>> {
        val window = hanningWindow(windowSize)
        val step = windowSize - overlap
        val spectogram = mutableListOf<List<Float>>()
        var i = 0
        while (i < data.size - windowSize) {
            val windowed = data.subList(i, i + windowSize).mapIndexed { index, value -> value * window[index] }
            spectogram.add(windowed)
            i += step
        }
        return spectogram
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