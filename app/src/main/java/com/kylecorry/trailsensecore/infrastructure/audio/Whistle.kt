package com.kylecorry.trailsensecore.infrastructure.audio

class Whistle: SoundPlayer(ToneGenerator().getTone(3150))