package com.kylecorry.trailsensecore.infrastructure.audio

import com.kylecorry.andromeda.sound.SoundPlayer

class PinkNoise : SoundPlayer(PinkNoiseGenerator().getNoise(durationSeconds = 5))