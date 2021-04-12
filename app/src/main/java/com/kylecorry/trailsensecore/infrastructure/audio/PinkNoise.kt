package com.kylecorry.trailsensecore.infrastructure.audio

class PinkNoise : SoundPlayer(PinkNoiseGenerator().getNoise(durationSeconds = 5))