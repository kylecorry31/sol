package com.kylecorry.trailsensecore.infrastructure.audio

import android.media.AudioTrack
import kotlin.math.sin

class WhiteNoiseGenerator {

    private val soundGenerator = SoundGenerator()

    fun getNoise(sampleRate: Int = 64000, durationSeconds: Int = 1): AudioTrack {
        return soundGenerator.getSound(sampleRate, durationSeconds) {
            Math.random() * 2 - 1
        }
    }

}