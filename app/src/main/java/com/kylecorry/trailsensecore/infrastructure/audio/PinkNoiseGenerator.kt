package com.kylecorry.trailsensecore.infrastructure.audio

import android.media.AudioTrack
import com.kylecorry.andromeda.sound.SoundGenerator
import kotlin.math.abs
import kotlin.random.Random

class PinkNoiseGenerator {

    private val soundGenerator = SoundGenerator()

    fun getNoise(sampleRate: Int = 64000, durationSeconds: Int = 1): AudioTrack {
        var b0 = 1.0
        var b1 = 0.0
        var b2 = 0.0
        var b3 = 0.0
        var b4 = 0.0
        var b5 = 0.0
        var b6 = 0.0

        val random = Random(0)
        var noise = mutableListOf<Double>()
        val size = (durationSeconds + 1) * sampleRate
        for (i in 0 until size){
            val white = random.nextDouble()
            b0 = 0.99886 * b0 + white * 0.0555179
            b1 = 0.99332 * b1 + white * 0.0750759
            b2 = 0.96900 * b2 + white * 0.1538520
            b3 = 0.86650 * b3 + white * 0.3104856
            b4 = 0.55000 * b4 + white * 0.5329522
            b5 = -0.7616 * b5 - white * 0.0168980
            val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
            b6 = white * 0.115926
            noise.add(pink)
        }

        val min = noise.minOrNull() ?: 0.0
        val max = noise.maxOrNull() ?: 0.0

        noise = if(abs(min) > max)
            noise.map { it / abs(min) }.toMutableList()
        else
            noise.map { it / max }.toMutableList()

        return soundGenerator.getSound(sampleRate, durationSeconds) {
            noise[it + sampleRate]
        }
    }

}