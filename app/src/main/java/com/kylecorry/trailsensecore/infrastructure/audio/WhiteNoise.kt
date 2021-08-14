package com.kylecorry.trailsensecore.infrastructure.audio

import com.kylecorry.andromeda.sound.SoundPlayer

class WhiteNoise : SoundPlayer(WhiteNoiseGenerator().getNoise(durationSeconds = 5))