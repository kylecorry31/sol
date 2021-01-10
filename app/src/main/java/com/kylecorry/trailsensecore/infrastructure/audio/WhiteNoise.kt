package com.kylecorry.trailsensecore.infrastructure.audio

class WhiteNoise : SoundPlayer(WhiteNoiseGenerator().getNoise(durationSeconds = 5))