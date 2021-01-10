package com.kylecorry.trailsensecore.infrastructure.audio

interface ISoundPlayer {
    fun on()
    fun off()
    fun isOn(): Boolean
    fun release()
}