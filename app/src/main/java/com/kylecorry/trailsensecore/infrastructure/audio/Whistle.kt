package com.kylecorry.trailsensecore.infrastructure.audio

import com.kylecorry.andromeda.sound.SoundPlayer
import com.kylecorry.andromeda.sound.ToneGenerator

class Whistle: SoundPlayer(ToneGenerator().getTone(3150))