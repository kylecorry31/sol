package com.kylecorry.trailsensecore.infrastructure.audio

interface IWhistle {
    fun on()
    fun off()
    fun isOn(): Boolean
    fun release()
}