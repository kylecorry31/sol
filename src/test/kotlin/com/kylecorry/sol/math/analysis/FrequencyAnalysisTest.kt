package com.kylecorry.sol.math.analysis

import com.kylecorry.sol.math.ComplexNumber
import com.kylecorry.sol.tests.performanceTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.sin

class FrequencyAnalysisTest {

    @Test
    fun fft() {
        val data = listOf(0f, 1f, 2f, 1f, 0f, 1f, 0f, 1f)
        val calculated = FrequencyAnalysis.fft(data)
        val expected = listOf(
            ComplexNumber(6f, 0f),
            ComplexNumber(0f, -2f),
            ComplexNumber(-2f, 0f),
            ComplexNumber(0f, 2f),
            ComplexNumber(-2f, 0f),
            ComplexNumber(0f, -2f),
            ComplexNumber(-2f, 0f),
            ComplexNumber(0f, 2f)
        )

        assertEquals(expected.size, calculated.size)
        for (i in expected.indices) {
            complexEquals(expected[i], calculated[i], 0.001f)
        }
    }

    @Test
    fun ifft(){
        val data =
            List(1024) { sin(it.toFloat() / 1024f * 2 * Math.PI).toFloat() + 0.5f * sin(it.toFloat() / 1024f * 2 * Math.PI * 3).toFloat() }
        val fft = FrequencyAnalysis.fft(data)
        val ifft = FrequencyAnalysis.ifft(fft)
        for (i in data.indices){
            assertEquals(data[i], ifft[i], 0.001f)
        }
    }

    @Test
    fun getMostResonantFrequency() {
        // Sine wave with frequency 1 Hz (power of 2 size)
        val data = List(1024) { sin(it.toFloat() / 1024f * 2 * Math.PI).toFloat() }
        val frequency = FrequencyAnalysis.getMostResonantFrequency(data, 1024f)
        assertEquals(1f, frequency!!, 0.001f)

        // Empty should return null
        assertEquals(null, FrequencyAnalysis.getMostResonantFrequency(listOf(), 1024f))
    }

    @Test
    fun getMostResonantFrequencies() {
        // Sine wave with frequency of 1 Hz + half power sine wave with frequency of 3 Hz (power of 2 size)
        val data =
            List(1024) { sin(it.toFloat() / 1024f * 2 * Math.PI).toFloat() + 0.5f * sin(it.toFloat() / 1024f * 2 * Math.PI * 3).toFloat() }

        val frequencies = FrequencyAnalysis.getMostResonantFrequencies(data, 1024f, 2)
        assertEquals(1f, frequencies[0], 0.001f)
        assertEquals(3f, frequencies[1], 0.001f)
    }

    @Test
    fun getFrequencySpectrum() {
        // Sine wave with frequency 1 Hz (power of 2 size)
        val data = List(1024) { sin(it.toFloat() / 1024f * 2 * Math.PI).toFloat() }
        val spectrum = FrequencyAnalysis.getFrequencySpectrum(data, 1024f)
        assertEquals(1024, spectrum.size)
        assertEquals(0f, spectrum[0].first, 0.001f)
        assertEquals(0f, spectrum[0].second, 0.001f)
        assertEquals(1f, spectrum[1].first, 0.001f)
        assertEquals(512f, spectrum[1].second, 0.001f)
        assertEquals(-1f, spectrum[1023].first, 0.001f)
    }

    @Test
    fun getMagnitudeOfFrequency() {
        // Sine wave with frequency of 1 Hz + half power sine wave with frequency of 3 Hz (power of 2 size)
        val data =
            List(1024) { sin(it.toFloat() / 1024f * 2 * Math.PI).toFloat() + 0.5f * sin(it.toFloat() / 1024f * 2 * Math.PI * 3).toFloat() }
        val magnitude1 = FrequencyAnalysis.getMagnitudeOfFrequency(data, 1f, 1024f)
        assertEquals(512f, magnitude1, 0.001f)

        val magnitude2 = FrequencyAnalysis.getMagnitudeOfFrequency(data, 3f, 1024f)
        assertEquals(256f, magnitude2, 0.001f)
    }

    private fun complexEquals(expected: ComplexNumber, actual: ComplexNumber, tolerance: Float = 0.0001f) {
        assertEquals(expected.real, actual.real, tolerance)
        assertEquals(expected.imaginary, actual.imaginary, tolerance)
    }

//    @Test
//    fun fftPerformance(){
//        val data = List(1024) { sin(it.toFloat() / 1024f * 2 * Math.PI).toFloat() }
//        val runs = 100000
//        val twiddleFactors = FrequencyAnalysis.getTwiddleFactorsFFT(data.size)
//        performanceTest(runs){
//            FrequencyAnalysis.fft(data, twiddleFactors)
//        }
//    }
}